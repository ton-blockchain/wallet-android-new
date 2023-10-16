package org.ton.wallet.uicomponents.snackbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.view.isVisible
import org.ton.wallet.core.Res
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.uikit.*

internal class SnackBarView(context: Context) : LinearLayout(context) {

    private val imageView = RLottieImageView(context)
    private val titleTextView = TextView(context)
    private val messageTextView = TextView(context)

    init {
        // view
        background = Res.drawableColored(RUiKitDrawable.bkg_rect_rounded_6dp, Res.color(RUiKitColor.snackbar_background))
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        minimumHeight = Res.dp(56)
        setPadding(Res.dp(10), Res.dp(12), Res.dp(10), Res.dp(12))

        // image
        imageView.scaleType = ImageView.ScaleType.CENTER
        imageView.isVisible = false
        val imageSize = Res.dp(32)
        addView(imageView, imageSize, imageSize)

        // text
        val textLayout = LinearLayout(context)
        textLayout.orientation = VERTICAL

        titleTextView.includeFontPadding = false
        titleTextView.typeface = Res.font(RUiKitFont.roboto_medium)
        titleTextView.setTextColor(Res.color(RUiKitColor.common_white))
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        val titleLayoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        titleLayoutParams.marginEnd = Res.dp(8)
        textLayout.addView(titleTextView, titleLayoutParams)

        messageTextView.includeFontPadding = false
        messageTextView.typeface = Res.font(R.font.roboto_regular)
        messageTextView.setTextColor(Res.color(R.color.common_white))
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        val messageLayoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        messageLayoutParams.topMargin = Res.dp(2)
        messageLayoutParams.marginEnd = Res.dp(8)
        textLayout.addView(messageTextView, messageLayoutParams)

        val textLayoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textLayoutParams.marginStart = Res.dp(10)
        addView(textLayout, textLayoutParams)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
        titleTextView.isVisible = !title.isNullOrEmpty()
    }

    fun setMessage(message: String?) {
        messageTextView.text = message
        messageTextView.isVisible = !message.isNullOrEmpty()
    }

    fun setImage(drawable: Drawable?) {
        imageView.setImageDrawable(drawable)
        imageView.isVisible = drawable != null
    }

    fun prepare() {
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (titleTextView.isVisible) 14f else 15f)
    }
}