package org.ton.wallet.screen.controller.helper

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bluelinelabs.conductor.Controller

internal class ControllerBarsHelper(
    controller: Controller
) : Controller.LifecycleListener() {

    private lateinit var insetsController: WindowInsetsControllerCompat

    init {
        controller.addLifecycleListener(this)
    }

    override fun preCreateView(controller: Controller) {
        super.preCreateView(controller)
        val window = controller.activity?.window!!
        insetsController = WindowCompat.getInsetsController(window, window.decorView)
    }

    fun setStatusBarLight(isLight: Boolean) {
        insetsController.isAppearanceLightStatusBars = isLight
    }

    fun setNavigationBarLight(isLight: Boolean) {
        insetsController.isAppearanceLightNavigationBars = isLight
    }
}