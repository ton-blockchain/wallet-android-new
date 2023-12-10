package org.ton.wallet.uicomponents

import android.view.View
import androidx.core.math.MathUtils
import androidx.recyclerview.widget.RecyclerView
import org.ton.wallet.core.Res
import org.ton.wallet.uicomponents.view.AppToolbar

object BottomSheetHelper {

    private val threshold = Res.dp(16)

    fun connectAppToolbarWithScrollableView(toolbar: AppToolbar, view: View) {
        if (view is RecyclerView) {
            view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var cumulativeScroll = 0
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    cumulativeScroll += dy
                    toolbar.setShadowAlpha(getAlpha(cumulativeScroll))
                }
            })
        } else {
            val scrollChangeListener = View.OnScrollChangeListener { _, _, scrollY, _, _ ->
                toolbar.setShadowAlpha(getAlpha(scrollY))
            }
            scrollChangeListener.onScrollChange(view, 0, view.scrollY, 0, 0)
            view.setOnScrollChangeListener(scrollChangeListener)
        }
    }

    private fun getAlpha(scroll: Int): Float {
        return MathUtils.clamp(scroll.toFloat() / threshold, 0f, 1f)
    }
}