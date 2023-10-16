package org.ton.wallet.uicomponents.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.*
import androidx.core.view.ViewCompat.requestApplyInsets
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitStyle

abstract class BaseDialog(
    context: Context,
    private val isCancellable: Boolean = true
) : Dialog(context, RUiKitStyle.Dialog_Transparent),
    OnApplyWindowInsetsListener {

    private val rootLayout = FrameLayout(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window = window ?: throw IllegalStateException("Window is null")
        val windowParams = window.attributes
        windowParams.dimAmount = 0f
        windowParams.flags = windowParams.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        windowParams.gravity = Gravity.TOP or Gravity.START
        windowParams.width = MATCH_PARENT
        windowParams.height = MATCH_PARENT
        if (Build.VERSION.SDK_INT >= 28) {
            windowParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = windowParams
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, this)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        rootLayout.doOnAttach(::requestApplyInsets)
        rootLayout.setBackgroundColor(Res.color(RUiKitColor.dialog_background))
        if (isCancellable) {
            rootLayout.setOnClickListener { dismiss() }
        }
        setCancelable(isCancellable)
        setContentView(rootLayout, WindowManager.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        return insets
    }

    fun setView(view: View, layoutParams: ViewGroup.LayoutParams?) {
        rootLayout.addView(view, layoutParams)
    }
}