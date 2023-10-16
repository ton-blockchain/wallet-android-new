package org.ton.wallet.feature.wallet.impl.main

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.*
import androidx.core.math.MathUtils.clamp
import org.ton.wallet.core.Res
import org.ton.wallet.rlottie.RLottieDrawable
import org.ton.wallet.rlottie.RLottieResourceLoader
import org.ton.wallet.uicomponents.drawable.TopRoundRectDrawable
import org.ton.wallet.uikit.RUiKitRaw
import kotlin.math.roundToInt

internal class MainBottomSheetDrawable(context: Context) : TopRoundRectDrawable(), Drawable.Callback {

    private val handler = Handler(Looper.getMainLooper())
    private val bitmapPaint = Paint()
    private val path = Path()

    private var animationDrawable: RLottieDrawable? = null
    private var bitmap: Bitmap? = null
    private var drawableAnimationStartMs = 0L

    var animationOffset: Float = 0f
        private set

    init {
        RLottieResourceLoader.readRawResourceAsync(context, RUiKitRaw.lottie_loading) { json, _, _ ->
            animationDrawable = RLottieDrawable(json, RUiKitRaw.lottie_loading.toString(), Res.dp(100), Res.dp(100), true).apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                setAutoRepeat(1)
                start()
                callback = this@MainBottomSheetDrawable
            }
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updatePath()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.save()
        canvas.translate(0f, topOffset)
        bitmap?.let { bmp -> canvas.drawBitmap(bmp, 0f, 0f, bitmapPaint) }

        val animationProgress =
            if (drawableAnimationStartMs == 0L) 0f
            else clamp((SystemClock.elapsedRealtime() - drawableAnimationStartMs).toFloat() / AnimationDurationMs, 0f, 1f)
        animationDrawable?.let { drawable ->
            if (drawable.isRunning) {
                val top = animationOffset.roundToInt()
                val left = (bounds.width() - drawable.intrinsicWidth) / 2
                drawable.setBounds(left, top, left + drawable.intrinsicWidth, top + drawable.intrinsicHeight)
                drawable.alpha = (255 * (1f - animationProgress)).toInt()
                drawable.draw(canvas)
            }
        }

        canvas.restore()

        if (drawableAnimationStartMs > 0L) {
            invalidateSelf()
        }
    }

    override fun setTopRadius(radius: Float) {
        super.setTopRadius(radius)
        updatePath()
    }

    override fun invalidateDrawable(who: Drawable) {
        callback?.invalidateDrawable(this)
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = Unit

    override fun unscheduleDrawable(who: Drawable, what: Runnable) = Unit

    fun setAnimationAlpha(alpha: Int) {
        animationDrawable?.alpha = alpha
        invalidateSelf()
    }

    fun setAnimationOffset(offset: Float) {
        animationOffset = offset
        invalidateSelf()
    }

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        invalidateSelf()
    }

    fun setBitmapAlpha(alpha: Int) {
        bitmapPaint.alpha = alpha
        invalidateSelf()
    }

    fun showAnimation() {
        animationDrawable?.let { drawable ->
            drawable.start()
            drawable.alpha = 255
        }
        invalidateSelf()
    }

    fun hideAnimation() {
        drawableAnimationStartMs = SystemClock.elapsedRealtime()
        handler.postDelayed({
            animationDrawable?.stop()
            drawableAnimationStartMs = 0L
            invalidateSelf()
        }, AnimationDurationMs)
        invalidateSelf()
    }

    private fun updatePath() {
        if (bounds.width() * bounds.height() == 0 || topRadius == 0f) {
            return
        }
        path.reset()
        path.addRoundRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), topRadius, topRadius, Path.Direction.CW)
    }

    private companion object {
        private const val AnimationDurationMs = 200L
    }
}