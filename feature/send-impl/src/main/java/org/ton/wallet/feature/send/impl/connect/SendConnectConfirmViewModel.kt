package org.ton.wallet.feature.send.impl.connect

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.crypto.base64
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.util.FontSpan
import org.ton.wallet.data.core.model.MessageData
import org.ton.wallet.data.core.model.getText
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.domain.blockhain.api.GetAddressTypeUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectSendResponseUseCase
import org.ton.wallet.domain.transactions.api.GetSendFeeUseCase
import org.ton.wallet.domain.transactions.api.SendUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.send.api.SendConnectConfirmScreenApi
import org.ton.wallet.feature.send.impl.connect.vh.SendConnectHeaderItem
import org.ton.wallet.feature.send.impl.connect.vh.ShowDetailsItem
import org.ton.wallet.lib.log.L
import org.ton.wallet.lib.tonconnect.TonConnectApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.drawable.IndeterminateProgressDrawable
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uicomponents.vh.SectionDividerItem
import org.ton.wallet.uicomponents.vh.SettingsTextUiItem
import org.ton.wallet.uikit.*

class SendConnectConfirmViewModel(
    private val args: SendConnectConfirmScreenArguments
) : BaseViewModel() {

    private val getAddressTypeUseCase: GetAddressTypeUseCase by inject()
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase by inject()
    private val getSendFeeUseCase: GetSendFeeUseCase by inject()
    private val screenApi: SendConnectConfirmScreenApi by inject()
    private val sendUseCase: SendUseCase by inject()
    private val snackBarController: SnackBarController by inject()
    private val tonConnectSendResponseUseCase: TonConnectSendResponseUseCase by inject()

    private val request = (args.event.request as TonConnectApi.SendTransactionRequest)

    private var isResponseAlreadySent = false
    private var passCodeEntered = false
    private var sendJob: Job? = null

    private val messagesFlow = MutableStateFlow<List<MessageData>?>(null)
    private val feeFlow = MutableStateFlow<Long?>(null)
    private val isSendingFlow = MutableStateFlow(false)
    private val isSentFlow = MutableStateFlow(false)
    private val isExpandedFlow = MutableStateFlow(false)

    val stateFlow: Flow<SendConnectConfirmState> = combine(
        messagesFlow, feeFlow, isSendingFlow, isSentFlow, isExpandedFlow
    ) { messages, fee, isSending, isSent, isExpanded ->
        val adapterItems = mapMessagesToAdapterItems(messages ?: emptyList(), fee, isExpanded)
        val dataState = when {
            messages == null -> SendConnectConfirmState.DataState.Loading
            isSending -> SendConnectConfirmState.DataState.Sending
            isSent -> SendConnectConfirmState.DataState.Sent
            else -> SendConnectConfirmState.DataState.Default
        }
        SendConnectConfirmState(adapterItems, dataState)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val isSenderCorrect = request.from?.let { isSenderAddressCorrect(it) } ?: true
            if (!isSenderCorrect) {
                screenApi.navigateBack()
                return@launch
            }

            messagesFlow.value = try {
                request.messages.map(::mapTonConnectMessage)
            } catch (e: SnackBarMessageException) {
                snackBarController.showMessage(e.msg)
                screenApi.navigateBack()
                return@launch
            }

            feeFlow.value = getSendFeeUseCase.invoke(messagesFlow.value ?: emptyList())
        }
    }

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        if (!isStarted && !isPush && isEnter && passCodeEntered) {
            performSend()
        }
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == PassCodeEnterScreenApi.ResultKeyPassCodeEntered) {
            val purpose = args?.getString(PassCodeEnterScreenApi.ArgumentKeyPurpose)
            if (purpose == PassCodeEnterPurposeConfirmSend) {
                passCodeEntered = true
                screenApi.navigateBack()
            }
        }
    }

    override fun onDestroy() {
        sendJob?.cancel()
        super.onDestroy()
    }

    fun dismissDialog() {
        performSendDecline()
    }

    fun onCancelClicked() {
        performSendDecline()
        screenApi.navigateBack()
    }

    fun onConfirmClicked() {
        if (sendJob?.isActive == true || feeFlow.value == null) {
            return
        }
        if (isTransactionExpired()) {
            showTransactionExpiredError()
            return
        }
        passCodeEntered = false
        screenApi.navigateToPassCodeEnter(PassCodeEnterPurposeConfirmSend)
    }

    fun onShowDetailsClicked() {
        isExpandedFlow.value = true
    }

    private fun performSendDecline() {
        if (isResponseAlreadySent) {
            return
        }
        executeOnAppScope {
            val response = TonConnectApi.SendTransactionResponse.createError(
                id = args.event.eventId,
                code = TonConnectApi.ErrorCodeUserDeclinedConnection,
                message = TonConnectApi.ErrorMessageUserDeclinedConnection
            )
            tonConnectSendResponseUseCase.sendResponse(args.event.clientId, response)
        }
        isResponseAlreadySent = true
    }

    private fun performSend() {
        if (sendJob?.isActive == true) {
            return
        }
        if (isTransactionExpired()) {
            showTransactionExpiredError()
            return
        }

        isSendingFlow.value = true
        sendJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = sendUseCase.invoke(messagesFlow.value ?: return@launch)
                isSentFlow.value = true
                executeOnAppScope {
                    val externalMessageCell = CellRef(result.externalMessage).toCell(Message.tlbCodec(AnyTlbConstructor))
                    val base64Boc = base64(BagOfCells(externalMessageCell).toByteArray())
                    val response = TonConnectApi.SendTransactionResponse.createSuccess(id = args.event.eventId, boc = base64Boc)
                    tonConnectSendResponseUseCase.sendResponse(args.event.clientId, response)
                    isResponseAlreadySent = true
                }
                withContext(Dispatchers.Main) {
                    delay(1000L)
                    screenApi.navigateBack()
                }
            } catch (e: Exception) {
                snackBarController.showMessage(SnackBarMessage(
                    title = null,
                    message = getErrorMessage(e),
                    drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                ))
            } finally {
                isSendingFlow.value = false
            }
        }
    }

    private fun executeOnAppScope(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScopes.appScope.launch(Dispatchers.IO) {
            block.invoke(this)
        }
    }

    private fun isTransactionExpired(): Boolean {
        val validUntil = request.validUntil ?: return false
        return (System.currentTimeMillis() / 1000) > validUntil
    }

    private fun showTransactionExpiredError() {
        snackBarController.showMessage(SnackBarMessage(
            title = null,
            message = Res.str(RString.transaction_expired),
            drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
        ))
    }

    private suspend fun mapMessagesToAdapterItems(messages: List<MessageData>, fee: Long?, isExpanded: Boolean): List<Any> {
        val adapterItems = mutableListOf<Any>()

        val valueColor = Res.color(RUiKitColor.text_primary)
        val gemDrawable = Res.drawable(RUiKitDrawable.ic_gem_18)
        val isMultipleItems = messages.size > 1 && isExpanded

        var totalAmount: Long = 0
        messages.forEachIndexed { index, messageData ->
            val isNeedShowMessage = index == 0 || isMultipleItems
            if (isNeedShowMessage) {
                val recipientAddressType = getAddressTypeUseCase.getAddressType(messageData.destination)
                val recipientAddress = recipientAddressType?.ufAddress?.let(Formatter::getShortAddress) ?: messageData.destination
                val recipientAddressSequence = SpannableStringBuilder(recipientAddress)
                recipientAddressSequence.setSpan(FontSpan(Res.font(R.font.robotomono_regular)), 0, recipientAddressSequence.length, SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE)
                val recipientAddressResultSequence = Formatter.getBeautifiedShortString(recipientAddressSequence, Res.font(RUiKitFont.roboto_regular))
                val recipientItem = SettingsTextUiItem(id = 0, title = Res.str(RString.recipient), value = recipientAddressResultSequence, valueColor = valueColor)
                adapterItems.add(recipientItem)

                val amountItem = SettingsTextUiItem(id = 0, title = Res.str(RString.total_amount), value = Formatter.getFormattedAmount(messageData.amount, true), valueColor = valueColor, valueDrawableStart = gemDrawable)
                adapterItems.add(amountItem)

                val message = messageData.getText(null)
                if (message != null) {
                    val messageItem = SettingsTextUiItem(id = 0, title = Res.str(RString.message), value = message, valueColor = valueColor)
                    adapterItems.add(messageItem)
                }
                if (isMultipleItems) {
                    adapterItems.add(SectionDividerItem)
                }
            }

            totalAmount += messageData.amount
        }

        val totalAmountString = Formatter.getFormattedAmount(totalAmount)
        adapterItems.add(0, SendConnectHeaderItem(totalAmountString))

        if (messages.size > 1 && !isExpanded) {
            adapterItems.add(ShowDetailsItem(Res.str(RString.show_details)))
        }

        val feeAmount: String?
        val feeDrawable: Drawable?
        if (fee == null) {
            feeAmount = null
            val drawable = IndeterminateProgressDrawable(Res.dp(18))
            drawable.setColor(Res.color(RUiKitColor.blue))
            feeDrawable = drawable
        } else {
            feeAmount = Formatter.getFormattedAmount(fee)
            feeDrawable = gemDrawable
        }
        val feeItem = SettingsTextUiItem(id = 0, title = Res.str(RString.fee), value = feeAmount, valueColor = valueColor, valueDrawableStart = feeDrawable)
        adapterItems.add(feeItem)

        return adapterItems
    }

    @Throws(SnackBarMessageException::class)
    private fun mapTonConnectMessage(message: TonConnectApi.SendTransactionRequest.Message): MessageData {
        val payloadCell = message.payload?.let { payloadBase64 ->
            try {
                BagOfCells(base64(payloadBase64)).roots.first()
            } catch (e: Exception) {
                L.e(e)
                val snackBarMessage = SnackBarMessage(Res.str(RString.error), Res.str(RString.payload_incorrect), Res.drawable(RUiKitDrawable.ic_warning_32))
                throw SnackBarMessageException(snackBarMessage)
            }
        }

        val stateInitCell = message.stateInit?.let {stateInitBase64 ->
            try {
                val stateInitCell = BagOfCells(base64(stateInitBase64)).roots.first()
                StateInit.loadTlb(stateInitCell)
            } catch (e: Exception) {
                L.e(e)
                val snackBarMessage = SnackBarMessage(Res.str(RString.error), Res.str(RString.state_init_incorrect), Res.drawable(RUiKitDrawable.ic_warning_32))
                throw SnackBarMessageException(snackBarMessage)
            }
        }

        return MessageData.buildRaw(message.address, message.amount.toLong(), payloadCell, stateInitCell)
    }

    private suspend fun isSenderAddressCorrect(address: String): Boolean {
        val addressType = getAddressTypeUseCase.getAddressType(address)
        val ufAddress = addressType?.ufAddress
        val accountState = getCurrentAccountDataUseCase.getAccountState()
        if (ufAddress != null && accountState != null) {
            val rawAddress = AddrStd.parse(ufAddress)
            val currentAccountRawAddress = AddrStd.parse(accountState.address)
            return rawAddress == currentAccountRawAddress
        }
        return true
    }

    private companion object {

        private const val PassCodeEnterPurposeConfirmSend = "confirmSend"
    }

    private class SnackBarMessageException(val msg: SnackBarMessage) : Exception()
}