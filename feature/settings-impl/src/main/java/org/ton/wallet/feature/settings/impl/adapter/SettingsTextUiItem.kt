package org.ton.wallet.feature.settings.impl.adapter

import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.lib.lists.diff.DiffUtilItem
import org.ton.wallet.uikit.*

data class SettingsTextUiItem(
    override val id: Int,
    val title: String,
    var value: String? = null,
    @ColorInt
    val titleColor: Int = Color.TRANSPARENT
) : SettingsUiItem, DiffUtilItem {

    override fun areItemsTheSame(newItem: DiffUtilItem): Boolean {
        return newItem is SettingsTextUiItem && id == newItem.id
    }

    override fun areContentsTheSame(newItem: DiffUtilItem): Boolean {
        return newItem is SettingsTextUiItem && newItem == this
    }
}

class SettingsTextValueChangePayload(val value: String?)

class SettingsTextViewHolder(
    parent: ViewGroup,
    private val callback: SettingsTextItemCallback
) : RecyclerHolder<SettingsTextUiItem>(LinearLayout(parent.context)), View.OnClickListener {

    private val titleView = TextView(parent.context)
    private val valueView = TextView(parent.context)

    init {
        val rootLayout = itemView as LinearLayout
        rootLayout.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        rootLayout.orientation = LinearLayout.HORIZONTAL
        rootLayout.setBackgroundResource(RUiKitDrawable.ripple_rect)
        rootLayout.setOnClickListener(this)
        rootLayout.setPadding(Res.dp(20), Res.dp(16), Res.dp(20), Res.dp(16))

        val titleLayoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT)
        titleLayoutParams.weight = 1f
        titleView.includeFontPadding = false
        titleView.setTextColor(DefaultTextColor)
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        titleView.typeface = Res.font(RUiKitFont.roboto_regular)
        rootLayout.addView(titleView, titleLayoutParams)

        val valueLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        valueLayoutParams.marginStart = Res.dp(8)
        valueView.includeFontPadding = false
        valueView.setTextColor(Res.color(RUiKitColor.blue))
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        valueView.typeface = Res.font(RUiKitFont.roboto_regular)
        rootLayout.addView(valueView, valueLayoutParams)
    }

    override fun bind(item: SettingsTextUiItem) {
        super.bind(item)
        titleView.text = item.title
        titleView.setTextColor(if (item.titleColor == Color.TRANSPARENT) DefaultTextColor else item.titleColor)
        valueView.text = item.value
    }

    override fun bindPayload(payload: Any) {
        super.bindPayload(payload)
        when (payload) {
            is SettingsTextValueChangePayload -> {
                item.value = payload.value
                valueView.text = payload.value
            }
        }
    }

    override fun onClick(v: View?) {
        callback.onTextItemClicked(item)
    }

    private companion object {

        private val DefaultTextColor = Res.color(R.color.common_black)
    }
}

interface SettingsTextItemCallback {

    fun onTextItemClicked(item: SettingsTextUiItem) = Unit
}