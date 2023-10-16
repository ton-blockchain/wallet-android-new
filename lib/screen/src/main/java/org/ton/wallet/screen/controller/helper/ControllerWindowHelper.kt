package org.ton.wallet.screen.controller.helper

import android.app.Dialog
import android.view.View
import android.widget.PopupWindow
import com.bluelinelabs.conductor.Controller

internal class ControllerWindowHelper(controller: Controller) : Controller.LifecycleListener() {

    private var currentPopupWindow: PopupWindow? = null
    private var currentDialog: Dialog? = null

    init {
        controller.addLifecycleListener(this)
    }

    override fun preDetach(controller: Controller, view: View) {
        currentPopupWindow?.dismiss()
        currentPopupWindow = null
        currentDialog?.dismiss()
        currentDialog = null
        super.preDetach(controller, view)
    }

    fun showPopupWindow(popupWindow: PopupWindow?, anchorView: View) {
        setPopupWindow(popupWindow)
        popupWindow?.showAsDropDown(anchorView)
    }

    fun setPopupWindow(popupWindow: PopupWindow?) {
        currentPopupWindow?.dismiss()
        currentPopupWindow = popupWindow
    }

    fun showDialog(dialog: Dialog?) {
        currentDialog?.dismiss()
        currentDialog = dialog
        dialog?.show()
    }
}