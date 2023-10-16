package org.ton.wallet.uicomponents.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils.clamp
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitStyleable
import kotlin.math.roundToInt

class PassCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val appearanceInterpolator = OvershootInterpolator(3.0f)
    private val disappearanceInterpolator = DecelerateInterpolator(1.0f)

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    private var defaultBackgroundPaintColor = 0
    @ColorInt
    private var defaultFillPaintColor = 0

    private var dotsCount = 4
    private var filledDots = 0
    private var animationStartTimestamps = LongArray(dotsCount)

    init {
        var isDarkTheme = false
        val typedArray = context.obtainStyledAttributes(attrs, RUiKitStyleable.PassCodeView)
        try {
            isDarkTheme = typedArray.getBoolean(RUiKitStyleable.PassCodeView_passcode_dark, isDarkTheme)
        } finally {
            typedArray.recycle()
        }
        setDark(isDarkTheme)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = (Radius * 2 + OuterSpace * 2 + paddingTop + paddingBottom).roundToInt()
        val width = (Radius * 2 * dotsCount + (dotsCount - 1) * InnerSpace + OuterSpace * 2 + paddingLeft + paddingRight).roundToInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cy = paddingTop + (height - paddingTop - paddingBottom) * 0.5f
        var cx = paddingLeft + OuterSpace + Radius + backgroundPaint.strokeWidth * 0.5f

        var isAnyAnimationInProgress = false
        val currentTimeStamp = SystemClock.elapsedRealtime()
        for (i in 0 until dotsCount) {
            backgroundPaint.color = defaultBackgroundPaintColor
            fillPaint.color = defaultFillPaintColor

            val isAnimationInProgress = animationStartTimestamps[i] != 0L
            val isDotVisible = i < filledDots

            var foregroundRadius = Radius - Res.dp(1f)
            var fillRadius = if (isDotVisible) Radius else 0f

            if (isAnimationInProgress) {
                val progress = clamp((currentTimeStamp - animationStartTimestamps[i]).toFloat() / AnimationDuration, 0f, 1f)
                if (progress == 1f) {
                    animationStartTimestamps[i] = 0L
                }

                val colorProgress = clamp(progress * 2f, 0f, 1f)
                if (isDotVisible) {
                    fillRadius = Radius * appearanceInterpolator.getInterpolation(progress)
                    fillPaint.color = ColorUtils.blendARGB(defaultBackgroundPaintColor, defaultFillPaintColor, colorProgress)
                } else {
                    foregroundRadius = Radius * disappearanceInterpolator.getInterpolation(progress) - Res.dp(1f)
                    backgroundPaint.color = ColorUtils.blendARGB(defaultFillPaintColor, defaultBackgroundPaintColor, colorProgress)
                }
                isAnyAnimationInProgress = true
            }

            canvas.drawCircle(cx, cy, Radius, backgroundPaint)
            if (foregroundRadius > 0f) {
                canvas.drawCircle(cx, cy, foregroundRadius, foregroundPaint)
            }
            if (fillRadius > 0f) {
                canvas.drawCircle(cx, cy, fillRadius, fillPaint)
            }

            cx += Radius * 2 + InnerSpace
        }

        if (isAnyAnimationInProgress) {
            invalidate()
        }
    }

    fun setDotsCount(count: Int) {
        if (dotsCount == count) {
            return
        }
        dotsCount = count
        animationStartTimestamps = LongArray(dotsCount)
        requestLayout()
    }

    fun setFilledDots(count: Int, isAnimated: Boolean) {
        if (count > dotsCount) {
            return
        }
        if (isAnimated) {
            if (count > filledDots) {
                animationStartTimestamps[count - 1] = SystemClock.elapsedRealtime()
            } else {
                for (i in filledDots - 1 downTo count) {
                    animationStartTimestamps[i] = SystemClock.elapsedRealtime()
                }
            }
        }
        filledDots = count
        invalidate()
    }

    fun setDark(isDark: Boolean) {
        defaultBackgroundPaintColor =
            if (isDark) Res.color(RUiKitColor.passcode_background_dark)
            else Res.color(RUiKitColor.input_disabled)
        defaultFillPaintColor =
            if (isDark) Res.color(RUiKitColor.common_white)
            else Res.color(RUiKitColor.common_black)

        backgroundPaint.color = defaultBackgroundPaintColor
        foregroundPaint.color =
            if (isDark) Res.color(RUiKitColor.common_black)
            else Res.color(RUiKitColor.common_white)
        fillPaint.color = defaultFillPaintColor
    }

    private companion object {

        private val Radius = Res.dp(8f)
        private val InnerSpace = Res.dp(16)
        private val OuterSpace = Res.dp(12)
        private const val AnimationDuration = 250L
    }
}