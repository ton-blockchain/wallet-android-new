package org.ton.wallet.app.navigation.changehandler

import android.animation.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor

abstract class SlideChangeHandler(removesViewOnPush: Boolean) : AnimatorChangeHandler(500, removesViewOnPush) {

    var withAnimation: Boolean = true

    override fun saveToBundle(bundle: Bundle) {
        super.saveToBundle(bundle)
        bundle.putBoolean(KeyWithAnimation, withAnimation)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        withAnimation = bundle.getBoolean(KeyWithAnimation)
        super.restoreFromBundle(bundle)
    }

    override fun getAnimator(container: ViewGroup, from: View?, to: View?, isPush: Boolean, toAddedToContainer: Boolean): Animator {
        val set = AnimatorSet()
        if (!withAnimation) {
            return set
        }

        val topControllerView = if (isPush) to else from
        topControllerView?.let { controller ->
            val translationStart =
                if (isPush && toAddedToContainer) controller.width.toFloat()
                else controller.translationX
            val translationEnd = if (isPush) 0f else controller.width.toFloat()
            val translationAnimator = ObjectAnimator.ofFloat(controller, View.TRANSLATION_X, translationStart, translationEnd)
            set.play(translationAnimator)
        }

        val bottomController = if (isPush) from else to
        bottomController?.let { controller ->
            val translationStart =
                if (isPush && toAddedToContainer) 0f
                else -controller.width * 0.5f
            val translationEnd =
                if (isPush) -controller.width * 0.5f
                else 0f
            val translationAnimator = ObjectAnimator.ofFloat(controller, View.TRANSLATION_X, translationStart, translationEnd)
            set.play(translationAnimator)

            val dimColor = Res.color(RUiKitColor.screen_dim_color)
            val startColor = if (isPush) Color.TRANSPARENT else dimColor
            val endColor = if (isPush) dimColor else Color.TRANSPARENT
            val colorDrawable = ColorDrawable(startColor)
            controller.foreground = colorDrawable

            val dimAnimator = ValueAnimator.ofArgb(startColor, endColor)
            dimAnimator.addUpdateListener { animator ->
                colorDrawable.color = animator.animatedValue as Int
            }
            set.play(dimAnimator)
        }

        set.interpolator = DecelerateInterpolator(2.0f)
        return set
    }

    override fun resetFromView(from: View) {
        from.translationX = 0f
    }

    companion object {

        private const val KeyWithAnimation = "withAnimation"

        fun create(removesFromViewOnPush: Boolean): SlideChangeHandler {
            return if (removesFromViewOnPush) RemovesViewSlideChangeHandler()
            else KeepViewsSlideChangeHandler()
        }
    }
}

class RemovesViewSlideChangeHandler : SlideChangeHandler(true)

class KeepViewsSlideChangeHandler : SlideChangeHandler(false)