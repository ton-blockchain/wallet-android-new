package org.ton.wallet.uicomponents.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.updatePadding
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitFont

class NumericTextView(context: Context) : AppCompatTextView(context) {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var descentAscentDiff: Float
    private var maxTextWidth = 0f
    private var textCenterY = 0f
    private var text = ""
    var textWidth = 0f
        private set

    init {
        textPaint.color = Res.color(RUiKitColor.text_secondary)
        textPaint.typeface = Res.font(RUiKitFont.roboto_regular)
        descentAscentDiff = textPaint.descent() + textPaint.ascent()
        updatePadding(left = Res.dp(30))
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val x = if (maxTextWidth == 0f) 0f else maxTextWidth - textWidth
        val y = height * 0.5f - textCenterY
        canvas.drawText(text, x, y, textPaint)
    }

    fun setNumber(number: Int) {
        text = "${number}."
        textPaint.textSize = textSize

        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        textCenterY = textBounds.exactCenterY()
        textWidth = textBounds.width().toFloat()
    }

    fun setMaxTextWidth(maxTextWidth: Float) {
        this.maxTextWidth = maxTextWidth
    }
}