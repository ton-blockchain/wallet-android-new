package org.ton.wallet.feature.settings.impl.adapter

import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.lib.lists.diff.DiffUtilItem
import org.ton.wallet.uicomponents.vh.SettingsUiItem
import org.ton.wallet.uicomponents.view.SwitchView
import org.ton.wallet.uikit.*

data class SettingsSwitchItem(
    override val id: Int,
    val title: String,
    var isChecked: Boolean
) : SettingsUiItem {

    override fun areItemsTheSame(newItem: DiffUtilItem): Boolean {
        return newItem is SettingsSwitchItem && id == newItem.id
    }

    override fun areContentsTheSame(newItem: DiffUtilItem): Boolean {
        return newItem is SettingsSwitchItem && newItem == this
    }

    override fun getChangePayload(newItem: DiffUtilItem): Any? {
        return if (newItem is SettingsSwitchItem && isChecked != newItem.isChecked) {
            SettingsSwitchCheckChangePayload(newItem.isChecked)
        } else {
            null
        }
    }
}

class SettingsSwitchCheckChangePayload(val isChecked: Boolean)

class SettingsSwitchViewHolder(
    parent: ViewGroup,
    private val callback: SettingsSwitchItemCallback
) : RecyclerHolder<SettingsSwitchItem>(FrameLayout(parent.context)), View.OnClickListener {

    private val textView = TextView(parent.context)
    private val switchItem = SwitchView(parent.context)

    init {
        val rootLayout = itemView as FrameLayout
        rootLayout.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        rootLayout.setBackgroundResource(RUiKitDrawable.ripple_rect)
        rootLayout.setOnClickListener(this)
        rootLayout.setPadding(Res.dp(20), Res.dp(16), Res.dp(18), Res.dp(16))

        textView.setTextColor(Res.color(RUiKitColor.common_black))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        textView.typeface = Res.font(RUiKitFont.roboto_regular)
        val textViewLayoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.CENTER_VERTICAL)
        textViewLayoutParams.marginEnd = SwitchView.Width + Res.dp(8)
        rootLayout.addView(textView, textViewLayoutParams)
        rootLayout.addView(switchItem, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER_VERTICAL or Gravity.END))
    }

    override fun bind(item: SettingsSwitchItem) {
        super.bind(item)
        textView.text = item.title
        switchItem.setChecked(item.isChecked, false)
    }

    override fun bindPayload(payload: Any) {
        super.bindPayload(payload)
        if (payload is SettingsSwitchCheckChangePayload) {
            item.isChecked = payload.isChecked
            switchItem.setChecked(payload.isChecked, true)
        }
    }

    override fun onClick(v: View?) {
        callback.onSwitchItemClicked(item)
    }
}

interface SettingsSwitchItemCallback {

    fun onSwitchItemClicked(item: SettingsSwitchItem) = Unit
}