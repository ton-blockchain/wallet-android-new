package org.ton.wallet.feature.onboarding.impl.base

import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.math.MathUtils
import androidx.core.view.*
import org.ton.wallet.core.Res
import org.ton.wallet.uicomponents.view.AppToolbar
import kotlin.math.max
import kotlin.math.roundToInt

internal class ToolbarSlidingHeaderController(
    private val toolbar: AppToolbar,
    private val scrollingView: ViewGroup,
    private val animationView: View,
    private val titleView: TextView
) : View.OnScrollChangeListener {

    private val titleTopSpace: Int
        get() = (titleView.layoutParams as ViewGroup.MarginLayoutParams).topMargin

    private val titleFullScrollHeight: Int
        get() = titleTopSpace - toolbar.titleTextTop

    private var totalScrollY = 0f

    init {
        toolbar.setTitleAlpha(0f)
        toolbar.setShadowAlpha(0f)
    }

    override fun onScrollChange(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (v != scrollingView) {
            return
        }

        totalScrollY += (scrollY - oldScrollY).toFloat()
        animationView.translationY = -totalScrollY
        titleView.translationY = max(-totalScrollY, -titleFullScrollHeight.toFloat())

        val startThreshold = toolbar.height * 0.5f
        val currentImportTitleTop = titleTopSpace + titleView.translationY
        val progress = MathUtils.clamp(1f - (currentImportTitleTop - toolbar.titleTextTop) / startThreshold, 0f, 1f)
        toolbar.setShadowAlpha(progress)
        animationView.alpha = 1f - progress

        val scale = 1f - progress * (1f - toolbar.titleTextSize / titleView.textSize)
        titleView.scaleX = scale
        titleView.scaleY = scale

        val toolbarTitleLeftDiff = titleView.left.toFloat() - toolbar.titleTextLeft
        val scaleWidthDiff = (1f - scale) * 0.5f * titleView.width
        titleView.translationX = -toolbarTitleLeftDiff * progress - scaleWidthDiff
    }

    fun onInsetsChanged(insets: WindowInsetsCompat) {
        val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val statusBarSize = statusBarInsets.top
        toolbar.updatePadding(top = statusBarSize)

        val topSpace = Res.dimenAttr(android.R.attr.actionBarSize) + statusBarSize
        val animationSize = Res.dp(100)
        val titleVerticalSpace = Res.dp(12)
        animationView.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = topSpace }
        titleView.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = topSpace + animationSize + titleVerticalSpace }

        val contentView = scrollingView.getChildAt(0)
        contentView.updatePadding(top = topSpace + animationSize + titleVerticalSpace * 2 + titleView.textSize.roundToInt())
        contentView.measure(MeasureSpec.makeMeasureSpec(Res.screenWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        if (Res.screenHeight < contentView.measuredHeight) {
            val screenContentHeightDiff = contentView.measuredHeight - Res.screenHeight
            if (screenContentHeightDiff < titleFullScrollHeight) {
                contentView.updatePadding(bottom = titleFullScrollHeight - screenContentHeightDiff)
            }
        }
    }
}