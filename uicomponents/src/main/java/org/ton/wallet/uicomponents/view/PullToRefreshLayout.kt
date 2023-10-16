package org.ton.wallet.uicomponents.view

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.TextPaint
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper.INVALID_POINTER
import org.ton.wallet.core.Res
import org.ton.wallet.strings.RString
import org.ton.wallet.uikit.*
import kotlin.math.*

class PullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val pullDrawable = PullDrawable()
    private val pullThreshold = Res.dp(60f)
    private val maxThreshold = Res.dp(72f)

    private var animator: ValueAnimator? = null
    private var activePointerId = INVALID_POINTER
    private var isDragging = false
    private var prevTranslation = 0f
    private var yDown = 0f
    private var yDownMotion = 0f

    private var listener: PullToRefreshListener? = null

    init {
        pullDrawable.callback = this
        setWillNotDraw(false)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who == pullDrawable || super.verifyDrawable(who)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pullDrawable.bounds.height() > 0) {
            pullDrawable.draw(canvas)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (canChildScrollUp()) {
            return false
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = ev.getPointerId(0)
                val pointerIndex = ev.findPointerIndex(activePointerId)
                yDown = ev.getY(pointerIndex)
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }
                val pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = ev.getY(pointerIndex)
                startDragging(y)
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER
                isDragging = false
            }
        }

        return isDragging
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (canChildScrollUp()) {
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = event.getY(pointerIndex)
                startDragging(y)
                if (isDragging) {
                    val scroll = y - yDownMotion
                    if (scroll > 0) {
                        val translation = min(scroll * 0.3f, maxThreshold)
                        if (prevTranslation <= pullThreshold && pullThreshold < translation) {
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            pullDrawable.setActive(isActive = true, isAnimated = true)
                        } else if (translation <= pullThreshold && pullThreshold < prevTranslation) {
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            pullDrawable.setActive(isActive = false, isAnimated = true)
                        }
                        setChildrenTranslation(translation)
                    } else {
                        return false
                    }
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_UP -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                if (isDragging) {
                    onScrollFinished()
                    isDragging = false
                }
                activePointerId = INVALID_POINTER
                return false
            }
        }

        return true
    }

    fun setPullToRefreshListener(listener: PullToRefreshListener?) {
        this.listener = listener
    }

    private fun canChildScrollUp(): Boolean {
        for (i in 0 until childCount) {
            if (getChildAt(i).canScrollVertically(-1)) {
                return true
            }
        }
        return false
    }

    private fun startDragging(y: Float) {
        if (y - yDown > touchSlop && !isDragging) {
            yDownMotion = yDown + touchSlop
            isDragging = true
        }
    }

    private fun onScrollFinished() {
        if (prevTranslation >= pullThreshold) {
            listener?.onRefresh()
        }
        animator?.cancel()
        animator = ValueAnimator.ofFloat(prevTranslation, 0f).apply {
            addUpdateListener { setChildrenTranslation(it.animatedValue as Float) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    pullDrawable.setActive(isActive = false, isAnimated = false)
                }
            })
            duration = 200
            interpolator = DecelerateInterpolator(2.0f)
            start()
        }
    }

    private fun setChildrenTranslation(translation: Float) {
        pullDrawable.setBounds(0, 0, width, translation.toInt())
        for (i in 0 until childCount) {
            getChildAt(i).translationY = translation
        }
        prevTranslation = translation
        invalidate()
    }

    fun interface PullToRefreshListener {

        fun onRefresh()
    }

    private class PullDrawable : Drawable() {

        private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG or TextPaint.LINEAR_TEXT_FLAG or TextPaint.SUBPIXEL_TEXT_FLAG)

        private val activeArrowBitmap = Res.drawableBitmap(Res.drawableColored(RUiKitDrawable.ic_arrow_pull, Res.color(RUiKitColor.pull_to_refresh_foreground)))
        private val inactiveArrowBitmap = Res.drawableBitmap(Res.drawableColored(RUiKitDrawable.ic_arrow_pull, Res.color(RUiKitColor.pull_to_refresh_background)))
        private val arrowMatrix = Matrix()
        private val swipeString = Res.str(RString.swipe_down_to_refresh)
        private val releaseString = Res.str(RString.release_to_refresh)
        private val swipeStringWidth: Float
        private val releaseStringWidth: Float

        private var isActive = false
        private var animationStartTimestamp = 0L

        init {
            inactivePaint.color = Res.color(RUiKitColor.pull_to_refresh_background)
            activePaint.color = Res.color(RUiKitColor.pull_to_refresh_foreground)
            linePaint.color = Res.color(RUiKitColor.common_white)
            textPaint.color = Res.color(RUiKitColor.common_white)
            textPaint.typeface = Res.font(RUiKitFont.roboto_medium)
            textPaint.textSize = Res.sp(15f)
            swipeStringWidth = textPaint.measureText(swipeString)
            releaseStringWidth = textPaint.measureText(releaseString)
        }

        override fun draw(canvas: Canvas) {
            if (bounds.height() > 0) {
                canvas.save()
                canvas.clipRect(0, 0, bounds.width(), bounds.height())
            }

            val isAnimationInProgress = SystemClock.elapsedRealtime() - animationStartTimestamp <= AnimationDuration
            var progress = when {
                isAnimationInProgress -> (SystemClock.elapsedRealtime() - animationStartTimestamp).toFloat() / AnimationDuration
                isActive -> 1f
                else -> 0f
            }
            if (isAnimationInProgress && !isActive) {
                progress = 1f - progress
            }
            val alphaFactor =
                if (bounds.height() > Res.dp(32)) 1f
                else bounds.height().toFloat() / Res.dp(32)

            // background
            val circleRadius = sqrt(bounds.width().toDouble() * bounds.width() + bounds.height() * bounds.height()).toFloat()
            val left = Res.dp(20f)
            val bottom = bounds.bottom.toFloat() - Res.dp(8f)
            if (isAnimationInProgress) {
                if (isActive) {
                    canvas.drawRect(bounds, inactivePaint)
                } else {
                    canvas.drawRect(bounds, activePaint)
                }
                val xCircle = left + Radius
                val yCircle = bottom - Radius
                if (isActive) {
                    canvas.drawCircle(xCircle, yCircle, circleRadius * progress, activePaint)
                } else {
                    canvas.drawCircle(xCircle, yCircle, circleRadius * (1f - progress), inactivePaint)
                }
            } else {
                if (isActive) {
                    canvas.drawRect(bounds, activePaint)
                } else {
                    canvas.drawRect(bounds, inactivePaint)
                }
            }

            // line
            val top = min(bounds.top.toFloat() + Res.dp(8f), bottom - Radius * 2f)
            linePaint.alpha = (0x1F * alphaFactor).roundToInt()
            canvas.drawRoundRect(left, top, left + Radius * 2, bottom, Radius, Radius, linePaint)
            linePaint.alpha = (0xFF * alphaFactor).roundToInt()
            canvas.drawCircle(left + Radius, bottom - Radius, Radius, linePaint)
            arrowMatrix.setTranslate(left + Radius - inactiveArrowBitmap.width * 0.5f, bottom - Radius * 2 + inactiveArrowBitmap.height * 0.5f)
            arrowMatrix.postRotate(-180f * progress, left + Radius, bottom - Radius)
            canvas.drawBitmap(if (isActive) activeArrowBitmap else inactiveArrowBitmap, arrowMatrix, null)

            // text
            val yText = bottom - Res.dp(6f) + textPaint.descent()
            if (progress < 1f) {
                textPaint.alpha = (0xFF * (1f - progress) * alphaFactor).roundToInt()
                if (progress != 0f) {
                    val scale = 0.9f + 0.1f * (1f - progress)
                    canvas.save()
                    canvas.scale(scale, scale, bounds.width() * 0.5f, bounds.height().toFloat())
                }
                canvas.drawText(swipeString, (bounds.width() - swipeStringWidth) * 0.5f, yText + (bottom - yText + textPaint.descent()) * progress, textPaint)
                if (progress != 0f) {
                    canvas.restore()
                }
            }
            if (progress > 0f) {
                textPaint.alpha = (0xFF * progress).roundToInt()
                if (progress != 1f) {
                    val scale = 0.9f + 0.1f * progress
                    canvas.save()
                    canvas.scale(scale, scale, bounds.width() * 0.5f, bounds.height().toFloat())
                }
                canvas.drawText(releaseString, (bounds.width() - releaseStringWidth) * 0.5f, yText - (bottom - yText + textPaint.descent()) * (1f - progress), textPaint)
                if (progress != 1f) {
                    canvas.restore()
                }
            }

            if (bounds.height() > 0) {
                canvas.restore()
            }
            if (isAnimationInProgress) {
                invalidateSelf()
            }
        }

        override fun setAlpha(alpha: Int) {
            inactivePaint.alpha = alpha
            activePaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            inactivePaint.colorFilter = colorFilter
            activePaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }

        fun setActive(isActive: Boolean, isAnimated: Boolean) {
            if (this.isActive == isActive) {
                return
            }
            this.isActive = isActive
            this.animationStartTimestamp =
                if (isAnimated) SystemClock.elapsedRealtime()
                else 0
        }

        private companion object {
            private const val AnimationDuration = 200f
            private val Radius = Res.dp(8f)
        }
    }
}