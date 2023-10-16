package org.ton.wallet.feature.wallet.impl.main.adapter

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.data.transactions.api.model.TransactionDto
import org.ton.wallet.domain.transactions.api.model.*
import org.ton.wallet.lib.lists.RecyclerAdapter
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.strings.RString
import org.ton.wallet.uikit.*

class MainTransactionsAdapter(
    private val callback: AdapterCallback
) : RecyclerAdapter<Any, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewTypeHeader -> TransactionHeaderViewHolder(parent)
            else -> TransactionItemViewHolder(parent, callback)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder is TransactionItemViewHolder) {
            holder.bind(getItemAt(position) as TransactionDataUiListItem, payloads)
        } else if (holder is TransactionHeaderViewHolder) {
            holder.bind(getItemAt(position) as TransactionHeaderUiListItem, payloads)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItemAt(position)) {
            is TransactionHeaderUiListItem -> ViewTypeHeader
            else -> ViewTypeTransaction
        }
    }

    fun setItemsWithAnimation(items: List<Any>, withAnimation: Boolean) {
        items.forEach {
            if (it is TransactionBaseUiListItem) {
                it.isVisible = !withAnimation
            }
        }
        setItems(items)
    }


    private companion object {
        private const val ViewTypeTransaction = 0
        private const val ViewTypeHeader = 1
    }


    private abstract class BaseViewHolder<T : TransactionBaseUiListItem>(view: View) : RecyclerHolder<T>(view) {

        override fun bind(item: T) {
            super.bind(item)
            itemView.alpha = if (item.isVisible) 1f else 0f
            if (!item.isVisible) {
                itemView.animate().alpha(1f).setDuration(150).start()
            }
        }
    }


    private class TransactionHeaderViewHolder(parent: ViewGroup) : BaseViewHolder<TransactionHeaderUiListItem>(TextView(parent.context)) {

        private val textView = itemView as TextView

        init {
            textView.setPadding(Res.dp(16), Res.dp(20), Res.dp(16), Res.dp(2))
            textView.setTextColor(Res.color(RUiKitColor.text_primary))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            textView.typeface = Res.font(RUiKitFont.roboto_medium)
        }

        override fun bind(item: TransactionHeaderUiListItem) {
            super.bind(item)
            textView.text = item.title
        }
    }

    private class TransactionItemViewHolder(
        parent: ViewGroup,
        private val callback: TransactionItemCallback,
    ) : BaseViewHolder<TransactionDataUiListItem>(ConstraintLayout(parent.context)), View.OnClickListener {

        private val imageView = ImageView(parent.context)
        private val valueText = TextView(parent.context)
        private val peerTypeText = TextView(parent.context)
        private val timeText = TextView(parent.context)
        private val peerAddressText = TextView(parent.context)
        private val feeText = TextView(parent.context)
        private val messageText = TextView(parent.context)

        init {
            val constraintLayout = itemView as ConstraintLayout
            constraintLayout.id = ViewCompat.generateViewId()
            constraintLayout.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            constraintLayout.foreground = Res.drawable(RUiKitDrawable.ripple_rect)
            constraintLayout.setBackgroundResource(RUiKitDrawable.bkg_transaction)
            constraintLayout.setOnClickListener(this)
            constraintLayout.setPadding(Res.dp(16), Res.dp(14), Res.dp(16), Res.dp(16))

            imageView.id = ViewCompat.generateViewId()
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageResource(RUiKitDrawable.ic_gem_18)
            constraintLayout.addView(imageView, ConstraintLayout.LayoutParams(Res.dp(18), Res.dp(18)))

            valueText.id = ViewCompat.generateViewId()
            valueText.includeFontPadding = false
            valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            valueText.typeface = Res.font(R.font.roboto_medium)
            constraintLayout.addView(valueText, ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

            peerTypeText.id = ViewCompat.generateViewId()
            peerTypeText.includeFontPadding = false
            peerTypeText.setTextColor(Res.color(R.color.text_secondary))
            peerTypeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            peerTypeText.typeface = Res.font(R.font.roboto_regular)
            constraintLayout.addView(peerTypeText, ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

            timeText.id = ViewCompat.generateViewId()
            timeText.includeFontPadding = false
            timeText.setTextColor(Res.color(R.color.text_secondary))
            timeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            timeText.typeface = Res.font(R.font.roboto_regular)
            constraintLayout.addView(timeText, ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

            peerAddressText.id = ViewCompat.generateViewId()
            peerAddressText.includeFontPadding = false
            peerAddressText.setTextColor(Res.color(R.color.text_primary))
            peerAddressText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            peerAddressText.typeface = Res.font(R.font.robotomono_regular)
            constraintLayout.addView(peerAddressText, ConstraintLayout.LayoutParams(0, WRAP_CONTENT))

            feeText.id = ViewCompat.generateViewId()
            feeText.includeFontPadding = false
            feeText.setTextColor(Res.color(R.color.text_secondary))
            feeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            feeText.typeface = Res.font(R.font.roboto_regular)
            constraintLayout.addView(feeText, ConstraintLayout.LayoutParams(0, WRAP_CONTENT))

            messageText.id = ViewCompat.generateViewId()
            messageText.includeFontPadding = false
            messageText.setBackgroundResource(RUiKitDrawable.bkg_transaction_msg)
            messageText.setTextColor(Res.color(R.color.text_primary))
            messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            messageText.typeface = Res.font(R.font.roboto_regular)
            val messageLayoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            messageLayoutParams.constrainedWidth = true
            messageLayoutParams.horizontalBias = 0.0f
            constraintLayout.addView(messageText, messageLayoutParams)

            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            constraintSet.connect(imageView.id, ConstraintSet.TOP, valueText.id, ConstraintSet.TOP)
            constraintSet.connect(imageView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(imageView.id, ConstraintSet.BOTTOM, valueText.id, ConstraintSet.BOTTOM)

            constraintSet.connect(valueText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(valueText.id, ConstraintSet.START, imageView.id, ConstraintSet.END, Res.dp(4))

            constraintSet.connect(peerTypeText.id, ConstraintSet.BASELINE, valueText.id, ConstraintSet.BASELINE)
            constraintSet.connect(peerTypeText.id, ConstraintSet.START, valueText.id, ConstraintSet.END, Res.dp(4))

            constraintSet.connect(timeText.id, ConstraintSet.BASELINE, peerTypeText.id, ConstraintSet.BASELINE)
            constraintSet.connect(timeText.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            constraintSet.connect(peerAddressText.id, ConstraintSet.TOP, valueText.id, ConstraintSet.BOTTOM, Res.dp(6))
            constraintSet.connect(peerAddressText.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(peerAddressText.id, ConstraintSet.END, timeText.id, ConstraintSet.START, Res.dp(8))

            constraintSet.connect(feeText.id, ConstraintSet.TOP, peerAddressText.id, ConstraintSet.BOTTOM, Res.dp(6))
            constraintSet.connect(feeText.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(feeText.id, ConstraintSet.END, timeText.id, ConstraintSet.START, Res.dp(8))

            constraintSet.connect(messageText.id, ConstraintSet.TOP, feeText.id, ConstraintSet.BOTTOM, Res.dp(10))
            constraintSet.connect(messageText.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(messageText.id, ConstraintSet.END, timeText.id, ConstraintSet.START, Res.dp(8))

            constraintSet.applyTo(constraintLayout)
        }

        override fun bind(item: TransactionDataUiListItem) {
            super.bind(item)

            valueText.text = item.value
            valueText.isVisible = item.value != null
            imageView.isVisible = item.value != null

            peerTypeText.text =
                if (item.value == null) null
                else if (item.type == TransactionDto.Type.In) Res.str(RString.from)
                else Res.str(RString.to)

            peerAddressText.text = item.peerAddressShort
            peerAddressText.isVisible = item.peerAddressShort != null
            timeText.text = item.timeString
            timeText.isVisible = item.timeString != null
            feeText.text = item.feeString
            feeText.isVisible = item.feeString != null
            messageText.text = item.messageText
            messageText.isVisible = !item.messageText.isNullOrEmpty()
        }

        override fun onClick(v: View?) {
            callback.onTransactionClicked(item)
        }
    }

    interface TransactionItemCallback {

        fun onTransactionClicked(transaction: TransactionDataUiListItem)
    }

    interface AdapterCallback : TransactionItemCallback
}