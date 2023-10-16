package org.ton.wallet.coreui.util

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class FontSpan(private val font: Typeface?) : TypefaceSpan("") {

    init {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.typeface = font
        updateMeasureState(textPaint)
    }

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint)
    }

    private fun applyCustomTypeFace(paint: TextPaint) {
        val font = font ?: return
        val oldStyle = paint.typeface?.style ?: 0
        val olsStyleFlag = oldStyle and font.style.inv()
        if (olsStyleFlag and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (olsStyleFlag and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = font
    }
}