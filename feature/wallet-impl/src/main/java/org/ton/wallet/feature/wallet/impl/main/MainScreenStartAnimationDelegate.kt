package org.ton.wallet.feature.wallet.impl.main

import android.animation.*
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.uicomponents.OnboardingAnimationHelper
import org.ton.wallet.uikit.RUiKitDimen
import kotlin.math.roundToInt

internal class MainScreenStartAnimationDelegate {

    private var screenAnimator: ValueAnimator? = null

    val isAnimating: Boolean
        get() = screenAnimator?.isRunning == true

    fun run(
        rootView: View,
        toolbarLayout: ViewGroup,
        recyclerView: RecyclerView,
        bottomSheetDrawable: MainBottomSheetDrawable,
        actionOnEnd: () -> Unit
    ) {
        bottomSheetDrawable.setBitmap(MainScreenController.BitmapForAnimation)
        bottomSheetDrawable.setTopOffset(0f)
        rootView.foreground = bottomSheetDrawable
        MainScreenController.BitmapForAnimation = null

        val targetOffset = Res.dimen(RUiKitDimen.splash_bottom_sheet_top)
        toolbarLayout.alpha = 0f
        toolbarLayout.pivotX = Res.screenWidth * 0.5f
        toolbarLayout.pivotY = targetOffset * 0.5f
        recyclerView.alpha = 0f
        recyclerView.pivotX = toolbarLayout.pivotX
        recyclerView.pivotY = toolbarLayout.pivotY

        screenAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                val bitmapAlpha = ((1f - progress) * 255).roundToInt()
                bottomSheetDrawable.setBitmapAlpha(bitmapAlpha)
                bottomSheetDrawable.setAnimationAlpha(255 - bitmapAlpha)
                bottomSheetDrawable.setTopOffset(targetOffset * progress)
                val scale = 0.7f + 0.3f * progress
                toolbarLayout.alpha = progress
                toolbarLayout.scaleX = scale
                toolbarLayout.scaleY = scale
                recyclerView.alpha = progress
                recyclerView.scaleX = scale
                recyclerView.scaleY = scale
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    screenAnimator = null
                    actionOnEnd.invoke()
                }
            })
            OnboardingAnimationHelper.applyAnimatorProperties(this)
        }
        screenAnimator?.start()
    }

    fun reset() {
        screenAnimator?.cancel()
        screenAnimator = null
    }

    private class MatchParentDrawable(
        private val drawable: Drawable
    ) : Drawable(), Drawable.Callback {

        init {
            drawable.callback = this
        }

        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
            super.setBounds(left, top, right, bottom)
            val l = (bounds.width() - drawable.intrinsicWidth) / 2
            val t = (bounds.height() - drawable.intrinsicHeight) / 2
            drawable.setBounds(l, t, l + drawable.intrinsicWidth, t + drawable.intrinsicHeight)
        }

        override fun draw(canvas: Canvas) {
            drawable.draw(canvas)
        }

        override fun setAlpha(alpha: Int) {
            drawable.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            drawable.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return drawable.opacity
        }

        override fun invalidateDrawable(who: Drawable) {
            invalidateSelf()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            scheduleSelf(what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            unscheduleSelf(what)
        }
    }
}