package org.ton.wallet.feature.transactions.impl

import android.app.Activity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.*
import org.ton.wallet.domain.transactions.api.model.TransactionDetailsState
import org.ton.wallet.feature.transactions.api.TransactionDetailsScreenApi
import org.ton.wallet.feature.transactions.impl.vh.TransactionDetailsHeaderItem
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.drawable.IndeterminateProgressDrawable
import org.ton.wallet.uicomponents.util.ClipboardController
import org.ton.wallet.uicomponents.vh.*
import org.ton.wallet.uikit.*
import org.ton.wallet.uikit.R

class TransactionDetailsViewModel(args: TransactionDetailsScreenArguments) : BaseViewModel() {

    private val clipboardController: ClipboardController by inject()
    private val transactionsRepository: TransactionsRepository by inject()
    private val screenApi: TransactionDetailsScreenApi by inject()

    private var messages: List<TransactionMessageDto>? = null

    private val transactionFlow = MutableStateFlow<TransactionDto?>(null)

    val stateFlow: Flow<TransactionDetailsState> = transactionFlow
        .filterNotNull()
        .map { transaction ->
            val adapterItems = getAdapterItems(transaction)

            val buttonTitle: String
            val isButtonPrimaryStyle: Boolean
            if (transaction.isMultiMessage) {
                buttonTitle = Res.str(RString.view_in_explorer)
                isButtonPrimaryStyle = false
            } else {
                buttonTitle =
                    if (transaction.status == TransactionStatus.Cancelled) Res.str(RString.send_ton_to_address_retry)
                    else Res.str(RString.send_ton_to_address)
                isButtonPrimaryStyle = true
            }

            TransactionDetailsState(adapterItems, buttonTitle, isButtonPrimaryStyle)
        }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                transactionFlow.value = transactionsRepository.getTransaction(args.internalId)
            } catch (e: Exception) {
                L.e(e, "Could not load transaction with internal id ${args.internalId}")
                screenApi.navigateBack()
            }
        }

        transactionsRepository.transactionsLocalIdChangedFlow
            .distinctUntilChanged()
            .filter { it == args.internalId }
            .onEach { transactionFlow.value = transactionsRepository.getTransaction(args.internalId) }
            .launchIn(viewModelScope + Dispatchers.IO)
    }

    fun onButtonClicked(activity: Activity) {
        val transaction = transactionFlow.value ?: return
        if (transaction.isMultiMessage) {
            onViewExplorerClicked(activity)
        } else {
            val peerAddress = transaction.getFirstAddress() ?: return
            val amount =
                if (transaction.status == TransactionStatus.Cancelled) transaction.getTotalAmount()
                else null
            screenApi.navigateToSendAmount(peerAddress, amount)
        }
    }

    fun onTextItemClicked(activity: Activity, item: SettingsTextUiItem) {
        val itemIndex = item.id / 10
        when (item.id % 10) {
            ItemAddress -> {
                messages?.getOrNull(itemIndex)?.address?.let { address ->
                    clipboardController.copyToClipboard(address, Res.str(RString.address_copied_to_clipboard), true)
                }
            }
            ItemHash -> {
                messages?.getOrNull(itemIndex)?.bodyHash?.let { hash ->
                    clipboardController.copyToClipboard(hash, Res.str(RString.transaction_hash_copied_to_clipboard), true)
                }
            }
            ItemViewInExplorer -> {
                onViewExplorerClicked(activity)
            }
        }
    }

    private fun onViewExplorerClicked(activity: Activity) {
        transactionFlow.value?.hash?.let { hash ->
            screenApi.navigateToBrowser(activity, getExplorerUrl(hash))
        }
    }

    private fun getAdapterItems(transaction: TransactionDto): List<Any> {
        val adapterItems = mutableListOf<Any>()

        // amount
        val amount = transaction.getTotalAmount() ?: 0
        val color =
            if (amount > 0L) Res.color(RUiKitColor.text_approve)
            else if (amount == 0L) Res.color(RUiKitColor.text_primary)
            else Res.color(RUiKitColor.text_error)

        // fee
        var feeString: String? = null
        val fee = transaction.fee
        if (fee != null && fee > 0L) {
            feeString = Res.str(RString.fee_transaction, Formatter.getFormattedAmount(fee))
        }

        // status
        val status: CharSequence = when (transaction.status) {
            TransactionStatus.Executed -> {
                val timestampSec = transaction.timestampSec
                val dateString = Formatter.getFullDateString(timestampSec * 1000)
                val timeString = Formatter.getTimeString(timestampSec * 1000)
                Res.str(RString.date_at_time, dateString, timeString)
            }
            TransactionStatus.Pending -> {
                val stringBuilder = SpannableStringBuilder(Res.str(RString.pending))
                stringBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.blue)), 0, stringBuilder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                stringBuilder
            }
            TransactionStatus.Cancelled -> {
                val stringBuilder = SpannableStringBuilder(Res.str(RString.cancelled))
                stringBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.text_error)), 0, stringBuilder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                stringBuilder
            }
        }
        val statusDrawable = if (transaction.status != TransactionStatus.Pending) {
            null
        } else {
            IndeterminateProgressDrawable(Res.dp(13)).apply {
                setStrokeWidth(Res.dp(1.5f))
                setColor(Res.color(RUiKitColor.blue))
            }
        }

        val headerItem = TransactionDetailsHeaderItem(
            amount = Formatter.getFormattedAmount(amount),
            amountColor = color,
            fee = feeString,
            status = status,
            statusDrawable = statusDrawable,
            message = if (transaction.isMultiMessage) null else transaction.getTextMessage()
        )
        adapterItems.add(headerItem)
        adapterItems.add(SettingsHeaderItem(Res.str(RString.details)))

        val allMessages = mutableListOf<TransactionMessageDto>()
        transaction.inMessage?.let(allMessages::add)
        transaction.outMessages?.forEach(allMessages::add)
        messages = allMessages

        val valueColor = Res.color(RUiKitColor.text_primary)
        val gemDrawable = Res.drawable(RUiKitDrawable.ic_gem_18)
        allMessages.forEachIndexed { index, message ->
            val prevAdapterItemsSize = adapterItems.size
            message.address?.let { address ->
                val recipientAddressSequence = Formatter.getBeautifiedAddressString(address, Res.font(R.font.robotomono_regular), Res.font(RUiKitFont.roboto_regular))
                val title =
                    if (transaction.type == TransactionDto.Type.In) Res.str(RString.sender)
                    else Res.str(RString.recipient)
                adapterItems.add(SettingsTextUiItem(id = index * 10 + ItemAddress, title = title, value = recipientAddressSequence, valueColor = valueColor))
            }
            if (transaction.isMultiMessage) {
                message.amount?.let { amount ->
                    val amountItem = SettingsTextUiItem(id = index * 10 + ItemAmount, title = Res.str(RString.total_amount), value = Formatter.getFormattedAmount(amount, true), valueColor = valueColor, valueDrawableStart = gemDrawable)
                    adapterItems.add(amountItem)
                }
                message.message?.let { messageText ->
                    val messageItem = SettingsTextUiItem(id = index * 10 + ItemMessage, title = Res.str(RString.message), value = messageText, valueColor = valueColor)
                    adapterItems.add(messageItem)
                }
            } else {
                message.bodyHash?.let { bodyHash ->
                    val value = Formatter.getMiddleAddress(bodyHash)
                    val hashItem = SettingsTextUiItem(id = index * 10 + ItemHash, title = Res.str(RString.transaction_title), value = value, valueColor = valueColor)
                    adapterItems.add(hashItem)
                }
                adapterItems.add(SettingsTextUiItem(id = index * 10 + ItemViewInExplorer, title = Res.str(RString.view_in_explorer), titleColor = Res.color(RUiKitColor.blue)))
            }
            if (adapterItems.size != prevAdapterItemsSize && index < allMessages.size - 1) {
                adapterItems.add(SectionDividerItem)
            }
        }

        return adapterItems
    }

    private fun getExplorerUrl(hash: String): String {
        return "https://tonscan.org/tx/$hash"
    }

    private companion object {
        private const val ItemAddress = 0
        private const val ItemAmount = 1
        private const val ItemMessage = 2
        private const val ItemHash = 3
        private const val ItemViewInExplorer = 4
    }
}