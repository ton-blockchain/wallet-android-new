package org.ton.wallet.uicomponents.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.text.InputType
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.*

class NumericEditTextLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val numberTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val clearImage = ImageView(context)
    val editText = AppEditText(context)

    private var textCenterY = 0f
    private var numberMaxTextWidth = 0f
    private var numberText = ""
    var numberTextWidth = 0f
        private set

    private var textFocusChangedListener: TextFocusChangedListener? = null

    init {
        numberTextPaint.color = Res.color(RUiKitColor.text_secondary)
        numberTextPaint.typeface = Res.font(RUiKitFont.roboto_regular)
        numberTextPaint.textSize = editText.paint.textSize

        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        editText.maxLines = 1
        editText.updatePadding(left = Res.dp(30), right = Res.dp(28))
        addView(editText, MATCH_PARENT, WRAP_CONTENT)

        clearImage.imageTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(Res.color(RUiKitColor.text_secondary), 127))
        clearImage.isVisible = false
        clearImage.setBackgroundResource(RUiKitDrawable.ripple_oval_dark)
        clearImage.scaleType = ImageView.ScaleType.CENTER
        clearImage.setImageResource(RUiKitDrawable.ic_clear_16)
        clearImage.setOnClickListener { editText.setText("") }
        val clearLayoutParams = LayoutParams(Res.dp(20), Res.dp(20), Gravity.CENTER_VERTICAL or Gravity.END)
        clearLayoutParams.marginEnd = Res.dp(4)
        addView(clearImage, clearLayoutParams)

        editText.addTextChangedListener { onTextOrFocusChanged() }
        editText.addOnFocusChangeListener { _, _ -> onTextOrFocusChanged() }

        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = if (numberMaxTextWidth == 0f) 0f else numberMaxTextWidth - numberTextWidth
        val y = height * 0.5f - textCenterY
        canvas.drawText(numberText, x, y, numberTextPaint)
    }

    fun setNumber(number: Int) {
        numberText = "${number}:"
        val textBounds = Rect()
        numberTextPaint.getTextBounds(numberText, 0, numberText.length, textBounds)
        textCenterY = textBounds.exactCenterY()
        numberTextWidth = textBounds.width().toFloat()
    }

    fun setMaxTextWidth(maxTextWidth: Float) {
        this.numberMaxTextWidth = maxTextWidth
    }

    fun setTextFocusChangedListener(listener: TextFocusChangedListener) {
        textFocusChangedListener = listener
    }

    private fun onTextOrFocusChanged() {
        textFocusChangedListener?.onTextFocusChanged(editText, editText.text ?: "", editText.hasFocus())
        val isClearVisible = editText.text?.isNotEmpty() == true && editText.hasFocus()
        animateClearButton(isClearVisible)
    }

    private fun animateClearButton(isVisible: Boolean) {
        if (clearImage.isVisible == isVisible) {
            return
        }
        if (isVisible) {
            clearImage.alpha = 0f
            clearImage.rotation = 90f
            clearImage.scaleX = 0.5f
            clearImage.scaleY = 0.5f
        }
        clearImage.animate().cancel()
        clearImage.animate()
            .alpha(if (isVisible) 1f else 0f)
            .rotation(if (isVisible) 0f else 90f)
            .scaleX(if (isVisible) 1.0f else 0.5f)
            .scaleY(if (isVisible) 1.0f else 0.5f)
            .setDuration(200L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    if (isVisible) {
                        clearImage.isVisible = true
                    }
                }
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isVisible) {
                        clearImage.isVisible = false
                    }
                }
            })
            .start()
    }

    interface TextFocusChangedListener {

        fun onTextFocusChanged(v: AppEditText, text: CharSequence, isFocused: Boolean)
    }
}