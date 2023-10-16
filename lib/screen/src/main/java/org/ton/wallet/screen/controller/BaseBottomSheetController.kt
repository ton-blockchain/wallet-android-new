package org.ton.wallet.screen.controller

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.*
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils.clamp
import androidx.core.view.*
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.containsMotionEvent
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.coreui.util.CubicBezierInterpolator
import org.ton.wallet.uikit.RUiKitDrawable
import kotlin.math.*

abstract class BaseBottomSheetController @JvmOverloads constructor(
    args: Bundle? = null
) : BaseController(args) {

    private val rootBackgroundDrawable = ColorDrawable(ColorUtils.setAlphaComponent(DimColor, 0))

    override val useBottomInsetsPadding = false

    protected open val isFullHeight: Boolean = false
    protected open val isAnimatedOpen: Boolean = true
    protected open val isAnimatedClose: Boolean = true

    private lateinit var bottomSheetLayout: BottomSheetLayout
    private lateinit var velocityTracker: VelocityTracker

    @State
    private var state = STATE_HIDDEN
    @StaticState
    private var staticState = STATE_HIDDEN
    private var showHideAnimator: ValueAnimator? = null
    private var isClosed = false

    protected val isShowHideAnimatorInProgress: Boolean
        get() = showHideAnimator?.isRunning == true

    override fun onPreCreateView() {
        super.onPreCreateView()
        velocityTracker = VelocityTracker.obtain()
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val rootLayout = FrameLayout(context)
        rootLayout.background = rootBackgroundDrawable
        rootLayout.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        rootLayout.setOnClickListenerWithLock(::close)

        bottomSheetLayout = BottomSheetLayout(context)

        val height = if (isFullHeight) MATCH_PARENT else WRAP_CONTENT
        val bottomSheetLayoutParams = FrameLayout.LayoutParams(MATCH_PARENT, height, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        rootLayout.addView(bottomSheetLayout, bottomSheetLayoutParams)

        val contentView = createBottomSheetView(inflater, bottomSheetLayout, savedViewState)
        bottomSheetLayout.addView(contentView)
        bottomSheetLayout.isInvisible = savedViewState == null

        setStatusBarLight(false)
        setNavigationBarLight(true)
        return rootLayout
    }

    abstract fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isPush && changeType.isEnter) {
            show()
        }
    }

    override fun handleBack(): Boolean {
        return if (isClosed) {
            super.handleBack()
        } else {
            close()
            true
        }
    }

    override fun onDestroyView(view: View) {
        velocityTracker.recycle()
        super.onDestroyView(view)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val superInsets = super.onApplyWindowInsets(v, insets)
        val systemBarsInsets = superInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeInsets = superInsets.getInsets(WindowInsetsCompat.Type.ime())

        val paddingBottom =
            if (imeInsets.bottom > 0) imeInsets.bottom
            else systemBarsInsets.bottom

        bottomSheetLayout.updatePadding(bottom = paddingBottom)

        return superInsets
    }

    protected open fun onAnimationFinished() = Unit

    private fun show() {
        if (isAnimatedOpen) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(Res.screenWidth, View.MeasureSpec.AT_MOST)
            val heightSpec =
                if (isFullHeight) View.MeasureSpec.makeMeasureSpec(Res.screenHeight, View.MeasureSpec.EXACTLY)
                else View.MeasureSpec.makeMeasureSpec(Res.screenHeight, View.MeasureSpec.AT_MOST)
            bottomSheetLayout.measure(widthSpec, heightSpec)
            bottomSheetLayout.translationY = bottomSheetLayout.measuredHeight.toFloat()
            bottomSheetLayout.isInvisible = false
            startBottomSheetAnimation(0f, AnimationSpeedPxPerMillis, ::onAnimationFinished)
        }
    }

    private fun close() {
        if (isAnimatedClose) {
            startBottomSheetAnimation(bottomSheetLayout.measuredHeight.toFloat(), AnimationSpeedPxPerMillis, actionClose)
        } else {
            actionClose.invoke()
        }
    }

    private fun startBottomSheetAnimation(
        targetTranslation: Float,
        velocityPixelsPerMillis: Float = DefaultAnimationSpeedPxPerMillis,
        actionOnEnd: (() -> Unit)? = null
    ) {
        val distance = abs(bottomSheetLayout.translationY - targetTranslation)
        showHideAnimator?.cancel()
        showHideAnimator = ValueAnimator.ofFloat(bottomSheetLayout.translationY, targetTranslation).apply {
            addUpdateListener { animation ->
                setBottomSheetTranslation(animation.animatedValue as Float)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    actionOnEnd?.invoke()
                }
            })
            interpolator = CubicBezierInterpolator.Default
            duration = (distance.toDouble() / velocityPixelsPerMillis).roundToLong()
            start()
        }
    }

    protected open fun setBottomSheetTranslation(translation: Float) {
        bottomSheetLayout.translationY = translation
        if (bottomSheetLayout.measuredHeight == 0) {
            return
        }
        val progress = 1f - (translation / bottomSheetLayout.measuredHeight)
        val alpha = clamp((progress * DimMaxAlpha).roundToInt(), 0, DimMaxAlpha)
        rootBackgroundDrawable.color = ColorUtils.setAlphaComponent(DimColor, alpha)
    }

    protected fun setState(@State newState: Int) {
        state = newState
        staticState =
            if (newState == STATE_HIDDEN || newState == STATE_HALF_EXPANDED || newState == STATE_EXPANDED) newState
            else staticState
    }

    private val actionClose: () -> Unit = {
        isClosed = true
        activity?.onBackPressed()
    }


    private companion object {

        private const val DimMaxAlpha = 0x88
        @ColorInt
        private const val DimColor = Color.BLACK
        @Px
        private val ActionVelocityThresholdPixelsPerMillis = Res.dp(2f)
        @Px
        private val DefaultAnimationSpeedPxPerMillis = Res.dp(2f)
        @Px
        private val AnimationSpeedPxPerMillis = DefaultAnimationSpeedPxPerMillis * 1.25f

        @IntDef(STATE_HIDDEN, STATE_DRAGGING, STATE_ANIMATING, STATE_EXPANDED, STATE_HALF_EXPANDED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class State

        @IntDef(STATE_HIDDEN, STATE_HALF_EXPANDED, STATE_EXPANDED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class StaticState

        const val STATE_HIDDEN = 0
        const val STATE_HALF_EXPANDED = 1
        const val STATE_EXPANDED = 2
        const val STATE_DRAGGING = 3
        const val STATE_ANIMATING = 4
    }


    private inner class BottomSheetLayout(context: Context) : FrameLayout(context) {

        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        private val maxWidth = Res.dp(500)
        private val bottomSheetDrawablePadding = Rect()

        private var xDown = 0f
        private var yDown = 0f
        private var xPrev = 0f
        private var yPrev = 0f

        init {
            val bottomSheetDrawable = Res.drawable(RUiKitDrawable.sheet_shadow_round)
            bottomSheetDrawable.getPadding(bottomSheetDrawablePadding)
            background = bottomSheetDrawable
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            var newWidthMeasureSpec = widthMeasureSpec
            if (widthSpec == MeasureSpec.AT_MOST || widthSpec == MeasureSpec.EXACTLY) {
                val newSpecSize =
                    if (widthSize > maxWidth) maxWidth
                    else widthSize + bottomSheetDrawablePadding.left + bottomSheetDrawablePadding.right
                newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(newSpecSize, widthSpec)
            }
            super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            val velocityEvent = MotionEvent.obtain(ev)
            velocityEvent.offsetLocation(0f, translationY)
            velocityTracker.addMovement(velocityEvent)
            return super.dispatchTouchEvent(ev)
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            var intercept = false
            if (ev.action == MotionEvent.ACTION_DOWN) {
                xDown = ev.rawX
                yDown = ev.rawY
            } else if (ev.action == MotionEvent.ACTION_MOVE) {
                val xDownDist = ev.rawX - xDown
                val yDownDist = ev.rawY - yDown

                var nestedScrollingChildCanScrollUp = false
                val isScrollUp = ev.rawY - yPrev > 0
                if (isScrollUp) {
                    val scrollingView = findChildScrollingView(ev)
                    if (scrollingView != null) {
                        nestedScrollingChildCanScrollUp = scrollingView.computeVerticalScrollOffset() > 0
                    }
                }

                val shouldDispatchEventToScrollingChild = isScrollUp && nestedScrollingChildCanScrollUp
                        || !isScrollUp && bottomSheetLayout.translationY <= 0f

                if (!shouldDispatchEventToScrollingChild) {
                    val distance = sqrt(xDownDist * xDownDist + yDownDist * yDownDist)
                    if (abs(distance) > touchSlop && abs(yDownDist) > abs(xDownDist)) {
                        intercept = true
                    }
                }
            }
            xPrev = ev.rawX
            yPrev = ev.rawY
            return intercept
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_MOVE) {
                val yPrevDist = event.rawY - yPrev
                setBottomSheetTranslation(max(0f, bottomSheetLayout.translationY + yPrevDist))
                setState(STATE_DRAGGING)
            } else if (event.action == MotionEvent.ACTION_UP
                || event.action == MotionEvent.ACTION_CANCEL
                || event.action == MotionEvent.ACTION_OUTSIDE
            ) {
                velocityTracker.computeCurrentVelocity(1)
                val velocityPixelsPerMillis = velocityTracker.yVelocity
                val isEnoughVelocityToAction = abs(velocityPixelsPerMillis) > ActionVelocityThresholdPixelsPerMillis
                val halfExpandedTranslation: Float? = null

                if (staticState == STATE_EXPANDED || halfExpandedTranslation == null) {
                    val velocity =
                        if (velocityPixelsPerMillis == 0f) DefaultAnimationSpeedPxPerMillis
                        else abs(velocityPixelsPerMillis).coerceAtLeast(DefaultAnimationSpeedPxPerMillis)
                    val translationToHide = min(bottomSheetLayout.measuredHeight / 3f, Res.dp(200f))
                    if (bottomSheetLayout.translationY > translationToHide || (isEnoughVelocityToAction && velocityPixelsPerMillis > 0)) {
                        startBottomSheetAnimation(bottomSheetLayout.measuredHeight.toFloat(), velocity, actionOnEnd = actionClose)
                    } else {
                        startBottomSheetAnimation(0f)
                    }
                } else if (staticState == STATE_HALF_EXPANDED) {
                    if (velocityPixelsPerMillis > 0) {
                        val translationToHide = halfExpandedTranslation + min((bottomSheetLayout.measuredHeight - halfExpandedTranslation) / 4, Res.dp(200f))
                        if (bottomSheetLayout.translationY > translationToHide || isEnoughVelocityToAction) {
                            startBottomSheetAnimation(bottomSheetLayout.measuredHeight.toFloat(), velocityPixelsPerMillis / 4, actionOnEnd = actionClose)
                        } else {
                            startBottomSheetAnimation(halfExpandedTranslation)
                        }
                    } else if (velocityPixelsPerMillis < 0) {
                        val translationToExpand = min(halfExpandedTranslation * 0.75f, Res.dp(200f))
                        if (bottomSheetLayout.translationY < translationToExpand || isEnoughVelocityToAction) {
                            startBottomSheetAnimation(0f, velocityPixelsPerMillis / 2)
                        } else {
                            startBottomSheetAnimation(halfExpandedTranslation)
                        }
                    }
                }
            }
            xPrev = event.rawX
            yPrev = event.rawY
            return true
        }

        private fun findChildScrollingView(ev: MotionEvent): ScrollingView? {
            var view: View? = this
            while (view is ViewGroup && view !is ScrollingView) {
                var isViewFound = false
                for (i in 0 until (view as ViewGroup).childCount) {
                    val childView = view.getChildAt(i)
                    if (childView.containsMotionEvent(this, ev)) {
                        isViewFound = true
                        view = childView
                        break
                    }
                }
                if (!isViewFound) {
                    view = null
                }
            }
            return if (view is ScrollingView) view else null
        }
    }
}