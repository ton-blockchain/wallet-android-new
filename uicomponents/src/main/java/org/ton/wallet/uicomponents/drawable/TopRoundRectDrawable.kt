package org.ton.wallet.uicomponents.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px

open class TopRoundRectDrawable(
    private val height: Int = 0
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var topOffset = 0f
        private set
    var topRadius = 0f
        private set

    override fun draw(canvas: Canvas) {
        val height = bounds.height().toFloat()
        val width = bounds.width().toFloat()
        canvas.drawRoundRect(0f, topOffset, width, topOffset + height, topRadius, topRadius, paint)
        canvas.drawRect(0f, topOffset + height - topRadius, topRadius, topOffset + height, paint)
        canvas.drawRect(width - topRadius, topOffset + height - topRadius, width, topOffset + height, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun getIntrinsicHeight(): Int {
        return if (height > 0) height else super.getIntrinsicHeight()
    }

    fun setColor(@ColorInt color: Int) {
        paint.color = color
        invalidateSelf()
    }

    open fun setTopRadius(@Px radius: Float) {
        this.topRadius = radius
        invalidateSelf()
    }

    fun setTopOffset(offset: Float) {
        this.topOffset = offset
        invalidateSelf()
    }
}