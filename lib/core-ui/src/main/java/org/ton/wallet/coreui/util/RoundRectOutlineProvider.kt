package org.ton.wallet.coreui.util

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

class RoundRectOutlineProvider(
    var radius: Float,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(left, top, view.width - right, view.height - bottom, radius)
    }
}