package org.ton.wallet.feature.transactions.impl.vh

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import org.ton.wallet.feature.transactions.impl.R
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.uicomponents.view.amount.AmountView

class TransactionDetailsHeaderItem(
    val amount: String,
    @ColorInt
    val amountColor: Int,
    val fee: String?,
    val status: CharSequence?,
    val statusDrawable: Drawable?,
    val message: String?
)

class TransactionDetailsHeaderViewHolder(
    parent: ViewGroup
) : RecyclerHolder<TransactionDetailsHeaderItem>(R.layout.item_transaction_details_header, parent) {

    private val amountView: AmountView = itemView.findViewById(R.id.itemTransactionDetailsHeaderAmountView)
    private val feeTextView: TextView = itemView.findViewById(R.id.itemTransactionDetailsHeaderFeeText)
    private val statusTextView: TextView = itemView.findViewById(R.id.itemTransactionDetailsHeaderStatusText)
    private val messageTextView: TextView = itemView.findViewById(R.id.itemTransactionDetailsMessageText)

    override fun bind(item: TransactionDetailsHeaderItem) {
        super.bind(item)
        amountView.setText(item.amount, false)
        amountView.setTextColor(item.amountColor)
        feeTextView.text = item.fee
        feeTextView.isVisible = item.fee != null
        statusTextView.text = item.status
        statusTextView.isVisible = item.status != null
        statusTextView.setCompoundDrawablesWithIntrinsicBounds(item.statusDrawable, null, null, null)
        messageTextView.text = item.message
        messageTextView.isVisible = item.message != null
    }
}