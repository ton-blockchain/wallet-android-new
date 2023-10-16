package org.ton.wallet.uicomponents.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import org.ton.wallet.core.Res
import org.ton.wallet.uicomponents.drawable.IndeterminateProgressDrawable
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDrawable

class IndeterminateProgressDialog(
    context: Context,
    isCancelable: Boolean
) : BaseDialog(context, isCancelable) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentView = ImageView(context)
        contentView.background = Res.drawableColored(RUiKitDrawable.bkg_rect_rounded_18dp, Res.color(RUiKitColor.progress_dialog_background))
        contentView.scaleType = ImageView.ScaleType.CENTER
        val drawable = IndeterminateProgressDrawable(Res.dp(48))
        drawable.setColor(Res.color(RUiKitColor.progress_dialog_color))
        drawable.setStrokeWidth(Res.dp(3f))
        contentView.setImageDrawable(drawable)

        setView(contentView, FrameLayout.LayoutParams(Res.dp(86), Res.dp(86), Gravity.CENTER))
    }
}