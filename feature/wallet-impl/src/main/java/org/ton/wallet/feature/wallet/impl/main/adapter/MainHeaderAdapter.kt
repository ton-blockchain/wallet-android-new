package org.ton.wallet.feature.wallet.impl.main.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.feature.wallet.impl.R
import org.ton.wallet.feature.wallet.impl.main.MainScreenAdapterHolder
import org.ton.wallet.lib.lists.RecyclerAdapter
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.util.ClipboardController
import org.ton.wallet.uicomponents.view.amount.AmountView

internal class MainHeaderAdapter(
    var callback: HeaderItemCallback?,
    var clipboardController: ClipboardController?
) : RecyclerAdapter<MainHeaderAdapter.HeaderItem, MainHeaderAdapter.ViewHolder>() {

    private val item = HeaderItem()

    var height = 0

    init {
        setItems(listOf(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent, height, callback, clipboardController)
    }

    override fun getItemViewType(position: Int): Int {
        return MainScreenAdapterHolder.ViewTypeMainHeader
    }

    fun setAddress(address: String?) {
        item.addressFull = address
        item.addressShort = Formatter.getShortAddressSafe(address)
        notifyItemChanged(0, PayloadAddressSet(false))
    }

    fun setBalance(balance: CharSequence?, isAnimated: Boolean) {
        item.balance = balance
        notifyItemChanged(0, PayloadBalanceSet(isAnimated))
    }

    class ViewHolder(
        parent: ViewGroup,
        height: Int,
        private val callback: HeaderItemCallback?,
        private val clipboardController: ClipboardController?
    ) : RecyclerHolder<HeaderItem>(R.layout.item_main_header, parent), View.OnClickListener {

        private val addressText: TextView = itemView.findViewById(R.id.itemHeaderAddressText)
        private val balanceText: AmountView = itemView.findViewById(R.id.itemHeaderBalanceText)

        init {
            itemView.findViewById<View>(R.id.itemHeaderAddressText).setOnClickListener(this)
            itemView.findViewById<View>(R.id.mainReceiveButtonBackground).setOnClickListener(this)
            itemView.findViewById<View>(R.id.mainSendButtonBackground).setOnClickListener(this)
            itemView.layoutParams.height = height
        }

        override fun bind(item: HeaderItem) {
            super.bind(item)
            addressText.text = item.addressShort
            balanceText.setText(item.balance, false)
        }

        override fun bindPayload(payload: Any) {
            super.bindPayload(payload)
            if (payload is PayloadAddressSet) {
                if (payload.isAnimated) {
                    addressText.text = item.addressShort
                    addressText.alpha = 0f
                    addressText.animate().cancel()
                    addressText.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start()
                } else {
                    addressText.text = item.addressShort
                }
            } else if (payload is PayloadBalanceSet) {
                balanceText.setText(item.balance, payload.isAnimated)
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.mainReceiveButtonBackground -> callback?.onReceiveClicked()
                R.id.mainSendButtonBackground -> callback?.onSendClicked()
                R.id.itemHeaderAddressText -> item.addressFull?.let { clipboardController?.copyToClipboard(it, Res.str(RString.address_copied_to_clipboard)) }
            }
        }
    }

    class HeaderItem(
        var addressFull: String? = null,
        var addressShort: String? = null,
        var balance: CharSequence? = null
    )

    class PayloadAddressSet(val isAnimated: Boolean)

    class PayloadBalanceSet(val isAnimated: Boolean)

    interface HeaderItemCallback {

        fun onReceiveClicked()

        fun onSendClicked()
    }
}