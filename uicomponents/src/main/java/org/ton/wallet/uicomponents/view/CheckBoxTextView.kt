package org.ton.wallet.uicomponents.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.math.MathUtils.clamp
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDrawable
import kotlin.math.abs
import kotlin.math.roundToInt

class CheckBoxTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private val checkBoxDrawable = CheckBoxDrawable(Res.dp(18))
    private var checkChangedListener: CheckChangedListener? = null

    var isChecked = false
        private set

    init {
        compoundDrawablePadding = Res.dp(12)
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = false
        setCompoundDrawablesWithIntrinsicBounds(checkBoxDrawable, null, null, null)
        setPadding(Res.dp(12), Res.dp(8), Res.dp(16), Res.dp(8))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setOnClickListener { setChecked(!isChecked) }
    }

    fun setChecked(isChecked: Boolean) {
        if (this.isChecked != isChecked) {
            this.isChecked = isChecked
            checkBoxDrawable.setChecked(isChecked, isAttachedToWindow)
            checkChangedListener?.onCheckChanged(isChecked)
        }
    }

    fun setCheckChangedListener(listener: CheckChangedListener) {
        checkChangedListener = listener
    }


    fun interface CheckChangedListener {

        fun onCheckChanged(isChecked: Boolean)
    }

    private class CheckBoxDrawable(
        private val size: Int
    ) : Drawable() {

        private val enabledPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val disabledPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

        private val strokeWidth = size * 0.09f
        private val outerRadius = size * 0.17f
        private val innerRadius = outerRadius - strokeWidth

        private val checkBitmap = Res.drawableBitmap(Res.drawableColored(RUiKitDrawable.ic_check_18, Res.color(RUiKitColor.common_white)))
        private val bitmapSrcRect = Rect(0, 0, checkBitmap.width, checkBitmap.height)
        private val bitmapDstRect = Rect()

        private var isChecked = false
        private var animationStartTimestamp = 0L

        init {
            enabledPaint.color = Res.color(RUiKitColor.blue)
            disabledPaint.color = Res.color(RUiKitColor.toggle_disabled)
            foregroundPaint.color = Res.color(RUiKitColor.common_white)
        }

        override fun draw(canvas: Canvas) {
            var stroke = strokeWidth
            var canvasScale = 1.0f

            val isAnimationInProgress = animationStartTimestamp != 0L && SystemClock.elapsedRealtime() - AnimationDuration < animationStartTimestamp
            if (isAnimationInProgress) {
                val progress = clamp((SystemClock.elapsedRealtime() - animationStartTimestamp).toFloat() / AnimationDuration, 0f, 1f)
                val firstHalfProgress = clamp(progress * 2f, 0f, 1f)
                val secondHalfProgress = clamp((progress - 0.5f) * 2f, 0f, 1f)

                canvasScale = 0.9f + abs(progress - 0.5f) * 0.1f
                val imageScale: Float
                if (isChecked) {
                    stroke =
                        if (progress > 0.5f) 0f
                        else strokeWidth + (bounds.width() - strokeWidth) * 0.5f * firstHalfProgress
                    imageScale = if (progress >= 0.5f) secondHalfProgress else 0f
                } else {
                    stroke =
                        if (progress < 0.5f) 0f
                        else strokeWidth + (bounds.width() - strokeWidth) * 0.5f * (1f - secondHalfProgress)
                    imageScale = if (progress < 0.5f) 1f - firstHalfProgress else 0f
                }
                bitmapDstRect.set(
                    (bounds.exactCenterX() - bounds.width() * 0.5f * imageScale).roundToInt(),
                    (bounds.exactCenterY() - bounds.height() * 0.5f * imageScale).roundToInt(),
                    (bounds.exactCenterX() + bounds.width() * 0.5f * imageScale).roundToInt(),
                    (bounds.exactCenterY() + bounds.height() * 0.5f * imageScale).roundToInt()
                )
            } else {
                if (isChecked) {
                    stroke = 0f
                }
                bitmapDstRect.set(bounds)
            }

            var restoreCount = -1
            if (canvasScale != 1.0f) {
                restoreCount = canvas.save()
                canvas.scale(canvasScale, canvasScale, bounds.width() * 0.5f, bounds.height() * 0.5f)
            }

            // background
            if (isChecked) {
                canvas.drawRoundRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), outerRadius, outerRadius, enabledPaint)
            } else {
                canvas.drawRoundRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), outerRadius, outerRadius, disabledPaint)
            }
            if (stroke > 0f) {
                canvas.drawRoundRect(stroke, stroke, bounds.width() - stroke, bounds.height() - stroke, innerRadius, innerRadius, foregroundPaint)
            }

            // image
            if (isChecked || isAnimationInProgress) {
                canvas.drawBitmap(checkBitmap, bitmapSrcRect, bitmapDstRect, bitmapPaint)
            }

            if (restoreCount != -1) {
                canvas.restoreToCount(restoreCount)
            }

            if (isAnimationInProgress) {
                invalidateSelf()
            } else {
                animationStartTimestamp = 0L
            }
        }

        override fun setAlpha(alpha: Int) {
            enabledPaint.alpha = alpha
            disabledPaint.alpha = alpha
            foregroundPaint.alpha = alpha
            bitmapPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            enabledPaint.colorFilter = colorFilter
            disabledPaint.colorFilter = colorFilter
            foregroundPaint.colorFilter = colorFilter
            bitmapPaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        override fun getIntrinsicWidth(): Int {
            return size
        }

        override fun getIntrinsicHeight(): Int {
            return size
        }

        fun setChecked(isChecked: Boolean, isAnimated: Boolean) {
            if (this.isChecked == isChecked) {
                return
            }
            if (isAnimated) {
                animationStartTimestamp = if (animationStartTimestamp == 0L) {
                    SystemClock.elapsedRealtime()
                } else {
                    val animationTime = SystemClock.elapsedRealtime() - animationStartTimestamp
                    SystemClock.elapsedRealtime() - (AnimationDuration - animationTime)
                }
            }
            this.isChecked = isChecked
            invalidateSelf()
        }

        private companion object {
            private const val AnimationDuration = 250
        }
    }
}