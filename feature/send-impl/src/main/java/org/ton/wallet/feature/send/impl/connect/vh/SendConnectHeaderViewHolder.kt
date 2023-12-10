package org.ton.wallet.feature.send.impl.connect.vh

import android.view.ViewGroup
import org.ton.wallet.core.Res
import org.ton.wallet.feature.send.impl.R
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.uicomponents.view.amount.AmountView
import org.ton.wallet.uikit.RUiKitColor

internal class SendConnectHeaderItem(
    val amount: String
)

internal class SendConnectHeaderViewHolder(
    parent: ViewGroup
) : RecyclerHolder<SendConnectHeaderItem>(R.layout.item_send_connect_header, parent) {

    private val amountView: AmountView = itemView.findViewById(R.id.itemSendConnectHeaderAmountView)

    init {
        amountView.setTextColor(Res.color(RUiKitColor.text_primary))
    }

    override fun bind(item: SendConnectHeaderItem) {
        super.bind(item)
        amountView.setText(item.amount, true)
    }
}