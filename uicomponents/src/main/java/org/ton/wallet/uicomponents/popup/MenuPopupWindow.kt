package org.ton.wallet.uicomponents.popup

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.view.updatePadding
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.*

class MenuPopupWindow(context: Context) : BasePopupWindow(ScrollView(context), MATCH_PARENT, WRAP_CONTENT), View.OnClickListener {

    private val scrollView = view as ScrollView
    private val itemsLayout = LinearLayout(context)
    private var items = emptyList<MenuPopupItem>()

    init {
        itemsLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(itemsLayout, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        isOutsideTouchable = true
    }

    override fun onClick(v: View) {
        val position = itemsLayout.indexOfChild(v)
        if (position in items.indices) {
            items[position].clickListener?.invoke()
            dismiss()
        }
    }

    fun setItems(menuItems: List<MenuPopupItem>): MenuPopupWindow {
        this.items = menuItems
        itemsLayout.removeAllViews()

        val horizontalPadding = Res.dp(20)
        val verticalPadding = Res.dp(13)
        val textColor = Res.color(RUiKitColor.text_primary)

        menuItems.forEachIndexed { index, item ->
            val textView = TextView(itemsLayout.context)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.maxLines = 1
            textView.setBackgroundResource(RUiKitDrawable.ripple_rect)
            textView.setOnClickListener(this)
            textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            if (index == 0) {
                textView.updatePadding(top = Res.dp(15))
            }
            if (index == menuItems.size - 1) {
                textView.updatePadding(bottom = Res.dp(15))
            }
            textView.setTextColor(textColor)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            textView.text = item.title
            textView.typeface = Res.font(RUiKitFont.roboto_regular)
            itemsLayout.addView(textView, MATCH_PARENT, WRAP_CONTENT)
        }
        return this
    }

    class MenuPopupItem(
        val title: CharSequence,
        val clickListener: (() -> Unit)? = null
    )
}