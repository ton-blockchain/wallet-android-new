package org.ton.wallet.screen.controller.helper

import android.view.View
import android.view.WindowManager
import com.bluelinelabs.conductor.Controller

class ControllerSecureHelper(controller: Controller) : Controller.LifecycleListener() {

    var isSecured: Boolean = false

    init {
        controller.addLifecycleListener(this)
    }

    override fun postAttach(controller: Controller, view: View) {
        super.postAttach(controller, view)
        if (isSecured) {
            controller.activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun preDetach(controller: Controller, view: View) {
        if (isSecured) {
            controller.activity?.window?.setFlags(0, WindowManager.LayoutParams.FLAG_SECURE)
        }
        super.preDetach(controller, view)
    }
}