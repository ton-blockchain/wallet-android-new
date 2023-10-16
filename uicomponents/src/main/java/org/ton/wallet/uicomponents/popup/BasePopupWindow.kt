package org.ton.wallet.uicomponents.popup

import android.animation.*
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.PopupWindow
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.util.RoundRectOutlineProvider
import org.ton.wallet.uikit.RUiKitDrawable
import kotlin.math.max

open class BasePopupWindow(
    protected val view: View,
    width: Int = WRAP_CONTENT,
    height: Int = WRAP_CONTENT
) : PopupWindow(FrameLayout(view.context), WRAP_CONTENT, WRAP_CONTENT) {

    protected val handler = Handler(Looper.getMainLooper())
    protected val rootView = FrameLayout(view.context)
    protected val backgroundPaddings = Rect()

    private var animator: ValueAnimator? = null

    init {
        (contentView as FrameLayout).addView(rootView, width, height)

        val backgroundDrawable = Res.drawable(RUiKitDrawable.popup_fixed_alert2)
        backgroundDrawable.getPadding(backgroundPaddings)
        rootView.background = backgroundDrawable

        handler.post {
            view.clipToOutline = true
            view.outlineProvider = RoundRectOutlineProvider(Res.dp(6f))
            val layoutParams = FrameLayout.LayoutParams(
                view.layoutParams?.width ?: FrameLayout.LayoutParams.WRAP_CONTENT,
                view.layoutParams?.height ?: FrameLayout.LayoutParams.WRAP_CONTENT
            )
            rootView.addView(view, layoutParams)
        }
    }

    fun setDismissListener(onDismissListener: OnDismissListener?): BasePopupWindow {
        super.setOnDismissListener(onDismissListener)
        return this
    }

    fun setMinimumWidth(minWidth: Int): BasePopupWindow {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        width = max(minWidth, view.measuredWidth)
        return this
    }

    open fun show(v: View): BasePopupWindow {
        val locationPoint = IntArray(2)
        v.getLocationInWindow(locationPoint)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)

        val x = locationPoint[0] - backgroundPaddings.left
        val y = locationPoint[1] - view.measuredHeight - backgroundPaddings.top
        showAtLocation(v, Gravity.NO_GRAVITY, x, y)
        animate(true)

        return this
    }

    open fun show(v: View, gravity: Int, yOffset: Int): BasePopupWindow {
        showAsDropDown(v, 0, yOffset, gravity)
        animate(true)
        return this
    }

    override fun showAsDropDown(anchor: View?) {
        show(anchor ?: return)
    }


    override fun dismiss() {
        animate(false)
    }

    private fun animate(isShow: Boolean) {
        if (isShow) {
            rootView.alpha = 0f
            rootView.scaleX = 0.5f
            rootView.scaleY = 0.5f
        }
        val from = if (isShow) 0f else 1f
        val to = if (isShow) 1f else 0f
        animator = ValueAnimator.ofFloat(from, to).apply {
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                rootView.alpha = progress
                if (isShow) {
                    val scale = 0.5f + progress * 0.5f
                    rootView.scaleX = scale
                    rootView.scaleY = scale
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isShow) {
                        this@BasePopupWindow.superDismiss()
                    }
                    animator = null
                }
            })
            interpolator = DecelerateInterpolator(2.0f)
            duration = 150L
            start()
        }
    }

    private fun superDismiss() {
        super.dismiss()
    }
}