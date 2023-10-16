package org.ton.wallet.app.navigation.changehandler

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler

class BottomSheetChangeHandler : AnimatorChangeHandler(false) {

    override fun getAnimator(container: ViewGroup, from: View?, to: View?, isPush: Boolean, toAddedToContainer: Boolean): Animator {
        val animator = AnimatorSet()
        animator.duration = 0
        return animator
    }

    override fun resetFromView(from: View) = Unit
}