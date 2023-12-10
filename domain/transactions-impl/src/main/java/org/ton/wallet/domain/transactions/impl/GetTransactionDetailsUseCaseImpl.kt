package org.ton.wallet.domain.transactions.impl

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.TransactionStatus
import org.ton.wallet.domain.transactions.api.GetTransactionDetailsUseCase
import org.ton.wallet.domain.transactions.api.model.TransactionDetailsState
import org.ton.wallet.strings.RString
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitFont

class GetTransactionDetailsUseCaseImpl(
    private val transactionsRepository: TransactionsRepository
) : GetTransactionDetailsUseCase {
    
    override suspend fun invoke(id: Long): TransactionDetailsState? {
        val transaction = transactionsRepository.getTransaction(id) ?: return null

        var amountSpannable: SpannableStringBuilder? = null
        val amount = transaction.getTotalAmount()
        Log.d("GetTransactionDetailsUseCaseImpl", "amount: $amount")
        if (amount != null) {
            val amountString = Formatter.getFormattedAmount(amount)
            amountSpannable = Formatter.getBeautifiedAmount(amountString)
            val color =
                if (amount >= 0) Res.color(RUiKitColor.text_approve)
                else Res.color(RUiKitColor.text_error)
            amountSpannable?.setSpan(ForegroundColorSpan(color), 0, amountString.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }

        var feeText: String? = null
        val fee = transaction.fee
        if (fee != null && fee > 0L) {
            feeText = Res.str(RString.fee_transaction, Formatter.getFormattedAmount(fee))
        }

        var dateText: String? = null
        val timestampSec = transaction.timestampSec
        if (timestampSec != null) {
            val dateString = Formatter.getFullDateString(timestampSec * 1000)
            val timeString = Formatter.getTimeString(timestampSec * 1000)
            dateText = Res.str(RString.date_at_time, dateString, timeString)
        }

        val buttonText =
            if (transaction.status == TransactionStatus.Cancelled) Res.str(RString.send_ton_to_address_retry)
            else Res.str(RString.send_ton_to_address)

        val hash = transaction.hash
        val hashShort =
            if (transaction.status == TransactionStatus.Pending) null
            else Formatter.getBeautifiedShortStringSafe(Formatter.getShortHash(hash), Res.font(RUiKitFont.roboto_regular))
        val peerShortAddress = Formatter.getBeautifiedShortStringSafe(
            shortString = Formatter.getShortAddressSafe(transaction.getFirstAddress()),
            font = Res.font(RUiKitFont.roboto_regular)
        )
        return TransactionDetailsState(
            hash = hash,
            status = transaction.status,
            type = transaction.type,
            amount = amount,
            amountString = amountSpannable,
            fee = feeText,
            date = dateText,
            message = transaction.getMessage(),
            peerAddress = transaction.getFirstAddress(),
            peerDns = null,
            peerShortAddress = peerShortAddress,
            hashShort = hashShort,
            buttonText = buttonText
        )
    }
}