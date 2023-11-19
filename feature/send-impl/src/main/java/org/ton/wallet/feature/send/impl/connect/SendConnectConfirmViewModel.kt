package org.ton.wallet.feature.send.impl.connect

import android.os.Bundle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.block.StateInit
import org.ton.boc.BagOfCells
import org.ton.crypto.base64
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.data.core.ton.TonWalletHelper
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.blockhain.api.GetAddressUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectSendResponseUseCase
import org.ton.wallet.domain.transactions.api.GetSendFeeUseCase
import org.ton.wallet.domain.transactions.api.SendUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.send.api.SendConnectConfirmScreenApi
import org.ton.wallet.lib.log.L
import org.ton.wallet.lib.tonconnect.TonConnectApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable

class SendConnectConfirmViewModel(
    private val args: SendConnectConfirmScreenArguments
) : BaseViewModel() {

    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase by inject()
    private val getAddressUseCase: GetAddressUseCase by inject()
    private val getSendFeeUseCase: GetSendFeeUseCase by inject()
    private val screenApi: SendConnectConfirmScreenApi by inject()
    private val sendUseCase: SendUseCase by inject()
    private val snackBarController: SnackBarController by inject()
    private val tonConnectSendResponseUseCase: TonConnectSendResponseUseCase by inject()
    private val walletRepository: WalletRepository by inject()

    private val request = (args.event.request as TonConnectApi.SendTransactionRequest)
    private val requestMessage = request.messages.firstOrNull()
        ?: throw IllegalArgumentException("Messages is empty")
    private val requestAmount: Long = requestMessage.amount.toLong()

    private var passCodeEntered = false
    private var sendJob: Job? = null
    private var messageData: MessageData? = null

    private val _stateFlow = MutableStateFlow(SendConnectConfirmState(amount = requestAmount, receiverUfAddress = "",))
    val stateFlow: Flow<SendConnectConfirmState> = _stateFlow

    init {
        viewModelScope.launch(Dispatchers.IO) {
            messageData = requestMessage.payload?.let { payloadBase64 ->
                try {
                    val payloadCell = BagOfCells(base64(payloadBase64)).roots.first()
                    MessageData.raw(payloadCell)
                } catch (e: Exception) {
                    null
                }
            }
            messageData?.let { _stateFlow.value = _stateFlow.value.copy(payload = TonWalletHelper.getMessageText(it, walletRepository.seed)) }

            val senderUfAddress = request.from?.let { address -> getAddressUseCase.getUfAddress(address) }
            if (senderUfAddress != null) {
                val currentAccountAddress = getCurrentAccountDataUseCase.getAccountState()?.address
                if (senderUfAddress != currentAccountAddress) {
                    screenApi.navigateBack()
                    return@launch
                }
            }
            _stateFlow.value = _stateFlow.value.copy(senderUfAddress = senderUfAddress)

            val receiverUfAddress = getAddressUseCase.getUfAddress(requestMessage.address) ?: requestMessage.address
            _stateFlow.value = _stateFlow.value.copy(receiverUfAddress = receiverUfAddress)

            // if stateInit is not null, check if it is correct
            requestMessage.stateInit?.let { stateInitBase64 ->
                try {
                    StateInit.loadTlb(BagOfCells(base64(stateInitBase64)).roots.first())
                } catch (e: Exception) {
                    L.e(e)
                    val message = SnackBarMessage(
                        title = Res.str(RString.error),
                        message = Res.str(RString.state_init_incorrect),
                        drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                    )
                    snackBarController.showMessage(message)
                    screenApi.navigateBack()
                }
            }

            val fee = getSendFeeUseCase.invoke(receiverUfAddress, requestAmount, messageData, requestMessage.stateInit)
            val feeString = Formatter.getFormattedAmount(fee)
            _stateFlow.value = _stateFlow.value.copy(feeString = "≈ $feeString TON")
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

    fun onCancelClicked() {
        executeOnAppScope {
            val response = TonConnectApi.SendTransactionResponse.createError(
                id = args.event.eventId,
                code = TonConnectApi.ErrorCodeUserDeclinedConnection,
                message = TonConnectApi.ErrorMessageUserDeclinedConnection
            )
            tonConnectSendResponseUseCase.sendResponse(args.event.clientId, response)
        }
        screenApi.navigateBack()
    }

    fun onConfirmClicked() {
        if (sendJob?.isActive == true || _stateFlow.value.feeString == null) {
            return
        }
        if (isTransactionExpired()) {
            showTransactionExpiredError()
            return
        }
        passCodeEntered = false
        screenApi.navigateToPassCodeEnter(PassCodeEnterPurposeConfirmSend)
    }

    private fun performSend() {
        if (sendJob?.isActive == true) {
            return
        }
        if (isTransactionExpired()) {
            showTransactionExpiredError()
            return
        }
        _stateFlow.value = _stateFlow.value.copy(isSending = true)
        sendJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                sendUseCase.invoke(_stateFlow.value.receiverUfAddress, requestAmount, messageData, requestMessage.stateInit)
                _stateFlow.value = _stateFlow.value.copy(isSent = true)
                executeOnAppScope {
                    val response = TonConnectApi.SendTransactionResponse.createSuccess(id = args.event.eventId, boc = "")
                    tonConnectSendResponseUseCase.sendResponse(args.event.clientId, response)
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
                _stateFlow.value = _stateFlow.value.copy(isSending = false)
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
        return System.currentTimeMillis() > validUntil
    }

    private fun showTransactionExpiredError() {
        snackBarController.showMessage(SnackBarMessage(
            title = null,
            message = Res.str(RString.transaction_expired),
            drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
        ))
    }

    private companion object {

        private const val PassCodeEnterPurposeConfirmSend = "confirmSend"
    }
}