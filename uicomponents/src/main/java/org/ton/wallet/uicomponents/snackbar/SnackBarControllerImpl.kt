package org.ton.wallet.uicomponents.snackbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.AnyThread
import androidx.core.view.setMargins
import org.ton.wallet.core.Res
import kotlin.math.min

class SnackBarControllerImpl : SnackBarController {

    private val handler = Handler(Looper.getMainLooper())

    private val snackBarPopupWindow by lazy {
        SnackBarPopupWindow(rootLayout.context)
    }

    private val isUiThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    private lateinit var rootLayout: FrameLayout

    @AnyThread
    override fun showMessage(message: SnackBarMessage) {
        if (snackBarPopupWindow.isShowing) {
            handler.removeCallbacksAndMessages(null)
            handler.post {
                snackBarPopupWindow.close {
                    handler.post {
                        showMessageInternal(message)
                    }
                }
            }
        } else {
            if (isUiThread) {
                showMessageInternal(message)
            } else {
                handler.post { showMessageInternal(message) }
            }
        }
    }

    @AnyThread
    override fun hideMessage() {
        if (isUiThread) {
            hideMessageInternal()
        } else {
            handler.post { hideMessageInternal() }
        }
    }

    fun setRootLayout(rootLayout: FrameLayout) {
        this.rootLayout = rootLayout
    }

    fun onDestroy() {
        if (snackBarPopupWindow.isShowing) {
            snackBarPopupWindow.dismiss()
        }
    }

    private fun showMessageInternal(message: SnackBarMessage) {
        snackBarPopupWindow.setMessage(message)
        snackBarPopupWindow.show(rootLayout) {
            handler.postDelayed(hideSnackBarViewRunnable, message.durationMs)
        }
    }

    private fun hideMessageInternal() {
        if (snackBarPopupWindow.isShowing) {
            handler.removeCallbacksAndMessages(null)
            snackBarPopupWindow.close()
        }
    }

    private val hideSnackBarViewRunnable = Runnable {
        snackBarPopupWindow.close()
    }

    private class SnackBarPopupWindow(context: Context) : PopupWindow(),
        PopupWindow.OnDismissListener {

        private val snackBarView = SnackBarView(context)
        private val margin = Res.dp(8)
        private var actionOnDismiss: (() -> Unit)? = null

        init {
            val rootLayout = FrameLayout(context)
            contentView = rootLayout
            width = min(Res.screenWidth, Res.dp(400))
            height = WindowManager.LayoutParams.WRAP_CONTENT

            snackBarView.setOnClickListener { close() }
            val snackBarLayoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            snackBarLayoutParams.setMargins(margin)
            rootLayout.addView(snackBarView, snackBarLayoutParams)
            setOnDismissListener(this)
        }

        override fun onDismiss() {
            actionOnDismiss?.invoke()
            actionOnDismiss = null
        }

        fun setMessage(message: SnackBarMessage) {
            snackBarView.setTitle(message.title)
            snackBarView.setMessage(message.message)
            snackBarView.setImage(message.drawable)
            snackBarView.prepare()
        }

        fun show(view: View, actionOnEnd: (() -> Unit)? = null) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            snackBarView.measure(widthSpec, heightSpec)
            snackBarView.translationY = snackBarView.measuredHeight.toFloat() + margin

            showAtLocation(view, Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 0)
            animateSnackBar(0f, actionOnEnd)
        }

        fun close(actionOnDismiss: (() -> Unit)? = null) {
            this.actionOnDismiss = actionOnDismiss
            animateSnackBar(snackBarView.measuredHeight.toFloat() + margin, ::dismiss)
        }

        private fun animateSnackBar(translation: Float, actionOnEnd: (() -> Unit)?) {
            snackBarView.animate().cancel()
            snackBarView.animate()
                .translationY(translation)
                .setInterpolator(DecelerateInterpolator(2.0f))
                .setDuration(200L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        actionOnEnd?.invoke()
                    }
                })
                .start()
        }
    }
}