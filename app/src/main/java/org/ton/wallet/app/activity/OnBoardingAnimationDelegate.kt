package org.ton.wallet.app.activity

import android.animation.*
import android.widget.FrameLayout
import org.ton.wallet.core.Res
import org.ton.wallet.uicomponents.OnboardingAnimationHelper
import org.ton.wallet.uicomponents.drawable.TopRoundRectDrawable
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDimen

internal class OnBoardingAnimationDelegate(
    private val rootView: FrameLayout,
    val animationType: MainActivityAnimationType
) {

    private val bottomSheetTranslation = Res.dimen(RUiKitDimen.splash_bottom_sheet_top)
    private val bottomSheetRadius = Res.dimen(RUiKitDimen.bottom_sheet_radius)
    private val bottomSheetDrawable = TopRoundRectDrawable()

    private var offsetAnimator: ValueAnimator? = null
    private var isOpenAnimationPlayed = false

    init {
        bottomSheetDrawable.setColor(Res.color(RUiKitColor.common_white))
        bottomSheetDrawable.setTopRadius(bottomSheetRadius)
        rootView.foreground = bottomSheetDrawable
    }

    fun startOpenAnimation(onAnimationEnd: () -> Unit) {
        if (isOpenAnimationPlayed) {
            return
        }
        isOpenAnimationPlayed = true
        bottomSheetDrawable.setTopOffset(bottomSheetTranslation)
        var targetTranslation: Float = bottomSheetTranslation
        var duration: Long = 0
        if (animationType == MainActivityAnimationType.BottomSheetUp) {
            targetTranslation = 0f
            duration = OnboardingAnimationHelper.AnimationDurationMs
        } else if (animationType == MainActivityAnimationType.BottomSheetDown) {
            targetTranslation = Res.screenHeight.toFloat()
            duration = 250L
        }
        animate(targetTranslation, duration, onAnimationEnd)
    }

    private fun animate(targetTranslation: Float, duration: Long, onAnimationEnd: () -> Unit) {
        offsetAnimator?.cancel()
        offsetAnimator = ValueAnimator.ofFloat(bottomSheetDrawable.topOffset, targetTranslation).apply {
            addUpdateListener { animator ->
                val offset = animator.animatedValue as Float
                bottomSheetDrawable.setTopOffset(offset)
                val radiusFactor =
                    if (offset > bottomSheetRadius * 4) 1f
                    else offset / (bottomSheetRadius * 4)
                bottomSheetDrawable.setTopRadius(bottomSheetRadius * radiusFactor)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    rootView.foreground = null
                    onAnimationEnd.invoke()
                }
            })
            OnboardingAnimationHelper.applyAnimatorProperties(this)
            setDuration(duration)
            start()
        }
    }
}