package org.ton.wallet.uicomponents.vh

import android.text.TextUtils
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import org.ton.wallet.core.Res
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.lib.lists.diff.DiffUtilItem
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitFont

class SettingsHeaderItem(val title: String) : DiffUtilItem {

    override fun areItemsTheSame(newItem: DiffUtilItem): Boolean {
        return newItem is SettingsHeaderItem
    }

    override fun areContentsTheSame(newItem: DiffUtilItem): Boolean {
        return newItem is SettingsHeaderItem && title == newItem.title
    }
}

class SettingsHeaderViewHolder(
    parent: ViewGroup
) : RecyclerHolder<SettingsHeaderItem>(TextView(parent.context)) {

    private val textView: TextView = itemView as TextView

    init {
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.maxLines = 1
        textView.setPadding(Res.dp(20), Res.dp(20), Res.dp(20), Res.dp(4))
        textView.setTextColor(Res.color(RUiKitColor.blue))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        textView.typeface = Res.font(RUiKitFont.roboto_medium)
    }

    override fun bind(item: SettingsHeaderItem) {
        super.bind(item)
        textView.text = item.title
    }
}