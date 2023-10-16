package org.ton.wallet.uicomponents.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils.clamp
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor

class SwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animationStartTimestamp = 0L
    private var progress = 0f

    var isChecked: Boolean = false
        private set

    init {
        paint.color = ColorDisabled
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(Width + paddingLeft + paddingRight, Height + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val isAnimationInProgress = SystemClock.elapsedRealtime() - animationStartTimestamp < AnimationDuration
        progress = if (isAnimationInProgress) {
            val animationProgress = clamp((SystemClock.elapsedRealtime() - animationStartTimestamp).toFloat() / AnimationDuration, 0f, 1f)
            if (isChecked) animationProgress
            else 1f - animationProgress
        } else {
            if (isChecked) 1f else 0f
        }

        val widthNoPadding = (width - paddingLeft - paddingRight).toFloat()
        val y = height * 0.5f
        val bkgRadius = Res.dp(7f)
        val bkgSpace = Res.dp(4f)
        paint.color = ColorUtils.blendARGB(ColorDisabled, ColorEnabled, progress)
        canvas.drawRoundRect(paddingLeft + bkgSpace, y - bkgRadius, widthNoPadding - bkgSpace, y + bkgRadius, bkgRadius, bkgRadius, paint)

        val outRadius = Res.dp(10f)
        val x = outRadius + (widthNoPadding - outRadius * 2) * progress
        canvas.drawCircle(paddingLeft + x, y, outRadius, paint)

        val inRadius = Res.dp(8f)
        paint.color = ColorCircle
        canvas.drawCircle(paddingLeft + x, y, inRadius, paint)

        if (isAnimationInProgress) {
            invalidate()
        }
    }

    fun setChecked(isChecked: Boolean, isAnimated: Boolean) {
        this.isChecked = isChecked
        if (isAnimated) {
            animationStartTimestamp = SystemClock.elapsedRealtime()
        }
        invalidate()
    }

    companion object {

        private val ColorCircle = Res.color(RUiKitColor.common_white)
        private val ColorDisabled = Res.color(RUiKitColor.toggle_disabled)
        private val ColorEnabled = Res.color(RUiKitColor.toggle_enabled)

        private const val AnimationDuration = 150L
        val Height = Res.dp(20)
        val Width = Res.dp(38)
    }
}