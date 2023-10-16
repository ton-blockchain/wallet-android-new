package org.ton.wallet.uicomponents.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import org.ton.wallet.core.Res

abstract class ProgressDrawable(
    private val size: Int? = null
) : Drawable() {

    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = Color.WHITE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = Res.dp(2f)
        paint.style = Paint.Style.STROKE
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    fun setColor(@ColorInt color: Int) {
        paint.color = color
        invalidateSelf()
    }

    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return size ?: super.getIntrinsicWidth()
    }

    override fun getIntrinsicHeight(): Int {
        return size ?: super.getIntrinsicHeight()
    }
}