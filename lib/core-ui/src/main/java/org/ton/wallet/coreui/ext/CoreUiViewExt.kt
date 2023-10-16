package org.ton.wallet.coreui.ext

import android.view.*
import android.view.animation.LinearInterpolator
import org.ton.wallet.coreui.internal.TimeoutLock
import kotlin.math.sin

private val ClickTimeoutLock = TimeoutLock(350)

fun View.animateShake(
    shakesCount: Int,
    amplitude: Float,
    durationMs: Long,
    scaleToOriginal: Boolean = false
) {
    this.animate().cancel()
    val animator = this.animate()
        .setUpdateListener { animator ->
            val value = animator.animatedValue as Float
            this.translationX = (sin(value * Math.PI * shakesCount) * amplitude).toFloat()
        }
        .setDuration(durationMs)
    if (scaleToOriginal) {
        animator.scaleX(1f).scaleY(1f)
    }
    animator.start()
}

fun View.animateBounce(
    bouncesCount: Int,
    amplitudeScale: Float,
    durationMs: Long
) {
    val scaleFactor = amplitudeScale - 1f
    this.animate().cancel()
    this.animate()
        .setUpdateListener { animator ->
            val value = animator.animatedFraction
            val scale = 1f + (sin(value * Math.PI * bouncesCount) * scaleFactor).toFloat()
            this.scaleX = scale
            this.scaleY = scale
        }
        .setDuration(durationMs)
        .setInterpolator(LinearInterpolator())
        .start()
}

fun View.containsMotionEvent(touchParentView: View, event: MotionEvent): Boolean {
    var viewLeft = left
    var viewTop = top

    var currentParentView = parent as? ViewGroup
    while (currentParentView != null) {
        viewLeft += currentParentView.left - currentParentView.scrollX
        viewTop += currentParentView.top - currentParentView.scrollY
        currentParentView =
            if (currentParentView == touchParentView) null
            else currentParentView.parent as? ViewGroup
    }

    return viewLeft + translationX <= event.x && event.x <= viewLeft + width + translationX
            && viewTop + translationY <= event.y && event.y <= viewTop + height + translationY
}

fun View.setOnClickListener(action: () -> Unit) {
    setOnClickListener { action() }
}

fun View.setOnClickListenerWithLock(clickListener: () -> Unit) {
    setOnClickListener(withLock(clickListener))
}

fun View.setOnClickListenerWithLock(clickListener: () -> Unit, lockTimeMs: Long) {
    setOnClickListener(withLock(clickListener, lockTimeMs))
}

private fun withLock(clickListener: () -> Unit, lockTimeMs: Long = 0): View.OnClickListener {
    return View.OnClickListener {
        if (!ClickTimeoutLock.checkAndMaybeLock(lockTimeMs)) {
            clickListener.invoke()
        }
    }
}