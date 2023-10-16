package org.ton.wallet.uicomponents

import android.animation.Animator
import org.ton.wallet.coreui.util.CubicBezierInterpolator

object OnboardingAnimationHelper {

    const val AnimationDurationMs = 500L

    fun applyAnimatorProperties(animator: Animator) {
        animator.duration = AnimationDurationMs
        animator.interpolator = CubicBezierInterpolator.EaseOutQuint
    }
}