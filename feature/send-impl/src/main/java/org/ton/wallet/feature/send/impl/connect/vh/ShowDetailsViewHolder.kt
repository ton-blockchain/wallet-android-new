package org.ton.wallet.feature.send.impl.connect.vh

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDrawable

class ShowDetailsItem(
    val title: String
)

class ShowDetailsViewHolder(
    parent: ViewGroup,
    private val callback: Callback
) : RecyclerHolder<ShowDetailsItem>(TextView(parent.context)), View.OnClickListener {

    private val textView: TextView = itemView as TextView

    init {
        textView.foreground = Res.drawable(RUiKitDrawable.ripple_rect)
        textView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textView.setBackgroundResource(RUiKitDrawable.bkg_section_divider)
        textView.setOnClickListener(this)
        textView.setPadding(Res.dp(20), Res.dp(12), Res.dp(20), Res.dp(28))
        textView.setTextColor(Res.color(RUiKitColor.blue))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    }

    override fun bind(item: ShowDetailsItem) {
        super.bind(item)
        textView.text = item.title
    }

    override fun onClick(v: View?) {
        callback.onShowDetailsClicked()
    }


    interface Callback {

        fun onShowDetailsClicked()
    }
}