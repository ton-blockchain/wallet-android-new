package org.ton.wallet.uicomponents

import android.view.View
import androidx.core.math.MathUtils
import org.ton.wallet.core.Res
import org.ton.wallet.uicomponents.view.AppToolbar

object BottomSheetHelper {

    private val threshold = Res.dp(16)

    fun connectAppToolbarWithScrollableView(toolbar: AppToolbar, view: View) {
        val scrollChangeListener = View.OnScrollChangeListener { _, _, scrollY, _, _ ->
            toolbar.setShadowAlpha(MathUtils.clamp(scrollY.toFloat() / threshold, 0f, 1f))
        }
        scrollChangeListener.onScrollChange(view, 0, view.scrollY, 0, 0)
        view.setOnScrollChangeListener(scrollChangeListener)
    }
}