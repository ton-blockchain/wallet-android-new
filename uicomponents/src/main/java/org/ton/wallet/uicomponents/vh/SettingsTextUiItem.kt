package org.ton.wallet.uicomponents.vh

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.lib.lists.RecyclerHolder
import org.ton.wallet.lib.lists.diff.DiffUtilItem
import org.ton.wallet.uikit.*

data class SettingsTextUiItem(
    override val id: Int,
    val title: CharSequence,
    var value: CharSequence? = null,
    @ColorInt
    val titleColor: Int? = null,
    @ColorInt
    val valueColor: Int? = null,
    val valueDrawableStart: Drawable? = null
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
    private val callback: SettingsTextItemCallback? = null
) : RecyclerHolder<SettingsTextUiItem>(FrameLayout(parent.context)), View.OnLayoutChangeListener, View.OnClickListener {

    private val titleView = TextView(parent.context)
    private val valueView = TextView(parent.context)

    init {
        val rootLayout = itemView as FrameLayout
        rootLayout.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        rootLayout.setBackgroundResource(RUiKitDrawable.ripple_rect)
        rootLayout.setPadding(Res.dp(20), Res.dp(16), Res.dp(20), Res.dp(16))
        if (callback != null) {
            rootLayout.setOnClickListener(this)
        }

        titleView.addOnLayoutChangeListener(this)
        titleView.gravity = Gravity.START
        titleView.includeFontPadding = false
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        titleView.typeface = Res.font(RUiKitFont.roboto_regular)
        rootLayout.addView(titleView, WRAP_CONTENT, WRAP_CONTENT)

        valueView.compoundDrawablePadding = Res.dp(5)
        valueView.includeFontPadding = false
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        valueView.typeface = Res.font(RUiKitFont.roboto_regular)
        val valueLayoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        valueLayoutParams.marginStart = Res.dp(8)
        valueLayoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        rootLayout.addView(valueView, valueLayoutParams)
    }

    override fun bind(item: SettingsTextUiItem) {
        super.bind(item)
        titleView.text = item.title
        titleView.setTextColor(item.titleColor ?: DefaultTextColor)

        valueView.compoundDrawablePadding = if (item.value == null) 0 else Res.dp(5)
        valueView.text = item.value
        valueView.setTextColor(item.valueColor ?: DefaultValueColor)
        valueView.setCompoundDrawablesWithIntrinsicBounds(item.valueDrawableStart, null, null, null)
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

    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (v == titleView && (oldRight - oldLeft) != (right - left)) {
            valueView.updateLayoutParams<MarginLayoutParams> { marginStart = v.width + Res.dp(8) }
            ThreadUtils.postOnMain { valueView.requestLayout() }
        }
    }

    override fun onClick(v: View?) {
        callback?.onTextItemClicked(item)
    }

    private companion object {

        private val DefaultTextColor = Res.color(RUiKitColor.common_black)
        private val DefaultValueColor = Res.color(RUiKitColor.blue)
    }
}

interface SettingsTextItemCallback {

    fun onTextItemClicked(item: SettingsTextUiItem) = Unit
}