package org.ton.wallet.uicomponents.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.core.view.*
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.*

class AlertDialog private constructor(
    context: Context,
    private val builder: Builder
) : BaseDialog(context, builder.isCancelable) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentLayoutDrawable = Res.drawable(RUiKitDrawable.popup_fixed_alert)
        val contentLayoutDrawablePadding = Rect()
        contentLayoutDrawable.getPadding(contentLayoutDrawablePadding)

        val contentLayout = LinearLayout(context)
        contentLayout.background = Res.drawable(RUiKitDrawable.popup_fixed_alert)
        contentLayout.orientation = LinearLayout.VERTICAL
        val contentHorizontalPadding = Res.dp(24)
        contentLayout.updatePaddingRelative(
            start = contentHorizontalPadding + contentLayoutDrawablePadding.left,
            top = Res.dp(22) + contentLayoutDrawablePadding.top
        )

        val contentLayoutWidth =
            if (Res.isLandscapeScreenSize) Res.dp(400)
            else MATCH_PARENT
        val contentLayoutParams = FrameLayout.LayoutParams(contentLayoutWidth, WRAP_CONTENT, Gravity.CENTER)
        contentLayoutParams.setMargins(Res.dp(20))
        setView(contentLayout, contentLayoutParams)

        val titleView = TextView(context)
        titleView.includeFontPadding = false
        titleView.setLineSpacing(Res.sp(3f), 1f)
        titleView.setTextColor(Res.color(RUiKitColor.text_primary))
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        titleView.typeface = Res.font(RUiKitFont.roboto_medium)
        titleView.text = builder.title
        titleView.isVisible = builder.title != null
        val titleLayoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        titleLayoutParams.marginEnd = contentHorizontalPadding
        titleLayoutParams.bottomMargin = Res.dp(12)
        contentLayout.addView(titleView, titleLayoutParams)

        val messageView = TextView(context)
        messageView.includeFontPadding = false
        messageView.setLineSpacing(Res.sp(1f), 1f)
        messageView.setTextColor(Res.color(RUiKitColor.text_primary))
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        messageView.typeface = Res.font(R.font.roboto_regular)
        messageView.text = builder.message
        messageView.isVisible = builder.message != null
        val messageLayoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        messageLayoutParams.marginEnd = contentHorizontalPadding
        contentLayout.addView(messageView, messageLayoutParams)

        val buttonsLayout = LinearLayout(context)
        val buttonsLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        buttonsLayoutParams.gravity = Gravity.END
        buttonsLayoutParams.topMargin = Res.dp(16)
        buttonsLayoutParams.bottomMargin = Res.dp(12)
        buttonsLayoutParams.marginEnd = Res.dp(8)
        contentLayout.addView(buttonsLayout, buttonsLayoutParams)

        builder.negativeButton?.let { (text, clickListener) ->
            val buttonView = getButtonView(text)
            buttonView.setOnClickListener { clickListener?.onClick(this, DialogInterface.BUTTON_NEGATIVE) }
            buttonsLayout.addView(buttonView, WRAP_CONTENT, WRAP_CONTENT)
        }

        builder.positiveButton?.let { (text, clickListener) ->
            val buttonView = getButtonView(text)
            buttonView.setOnClickListener { clickListener?.onClick(this, DialogInterface.BUTTON_POSITIVE) }
            buttonsLayout.addView(buttonView, WRAP_CONTENT, WRAP_CONTENT)
        }

        buttonsLayout.isVisible = buttonsLayout.childCount > 0
    }

    private fun getButtonView(text: CharSequence): TextView {
        val textView = TextView(context)
        textView.background = Res.drawable(R.drawable.ripple_rect_6dp_light)
        textView.setLineSpacing(Res.sp(2f), 1f)
        val horizontalPadding = Res.dp(20)
        val verticalPadding = Res.dp(12)
        textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        textView.setTextColor(Res.color(R.color.text_blue))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.text = text
        textView.typeface = Res.font(R.font.roboto_medium)
        return textView
    }

    class Builder(
        val title: String? = null,
        val message: String? = null,
        val positiveButton: Pair<String, DialogInterface.OnClickListener?>? = null,
        val negativeButton: Pair<String, DialogInterface.OnClickListener?>? = null,
        val isCancelable: Boolean = true
    ) {

        fun build(context: Context): AlertDialog {
            return AlertDialog(context, this)
        }
    }
}