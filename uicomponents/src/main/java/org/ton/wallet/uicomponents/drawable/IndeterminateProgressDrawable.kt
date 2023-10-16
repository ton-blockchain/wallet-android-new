package org.ton.wallet.uicomponents.drawable

import android.graphics.Canvas
import android.os.SystemClock
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

class IndeterminateProgressDrawable(size: Int? = null) : ProgressDrawable(size) {

    private val accelerateInterpolator = AccelerateInterpolator()
    private val decelerateInterpolator = DecelerateInterpolator()

    private var currentProgressTime = 0L
    private var lastDrawTimestampMs = 0L
    private var startAngle = 0f
    private var sweepAngle = 0f
    private var isCircleRising = false

    override fun draw(canvas: Canvas) {
        val currentTimestampMs = SystemClock.elapsedRealtime()
        val timeDiff = min(currentTimestampMs - lastDrawTimestampMs, 16)
        currentProgressTime += timeDiff
        if (currentProgressTime >= RISING_TIME_MS) {
            currentProgressTime = RISING_TIME_MS
        }
        lastDrawTimestampMs = currentTimestampMs

        startAngle += 360 * timeDiff / ROTATION_TIME_MS
        val offsetCount = (startAngle / 360).toInt()
        startAngle -= offsetCount * 360

        val progress = currentProgressTime.toFloat() / RISING_TIME_MS
        sweepAngle = MIN_ANGLE +
                if (isCircleRising) {
                    (MAX_ANGLE - MIN_ANGLE) * accelerateInterpolator.getInterpolation(progress)
                } else {
                    -MAX_ANGLE * (1f - decelerateInterpolator.getInterpolation(progress))
                }

        if (currentProgressTime == RISING_TIME_MS) {
            if (isCircleRising) {
                startAngle += MAX_ANGLE
                sweepAngle = -(MAX_ANGLE - MIN_ANGLE).toFloat()
            }
            isCircleRising = !isCircleRising
            currentProgressTime = 0
        }

        val strokeHalfWidth = paint.strokeWidth * 0.5f
        canvas.drawArc(
            bounds.left.toFloat() + strokeHalfWidth,
            bounds.top.toFloat() + strokeHalfWidth,
            bounds.right.toFloat() - strokeHalfWidth,
            bounds.bottom.toFloat() - strokeHalfWidth,
            startAngle, sweepAngle, false, paint)
        invalidateSelf()
    }

    private companion object {

        private const val MIN_ANGLE = 4
        private const val MAX_ANGLE = 270

        private const val ROTATION_TIME_MS = 2000L
        private const val RISING_TIME_MS = 700L
    }
}