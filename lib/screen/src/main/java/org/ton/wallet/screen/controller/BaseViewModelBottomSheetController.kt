package org.ton.wallet.screen.controller

import android.os.Bundle
import org.ton.wallet.screen.controller.helper.ControllerViewModelHelper
import org.ton.wallet.screen.viewmodel.BaseViewModel

abstract class BaseViewModelBottomSheetController<VM : BaseViewModel>(args: Bundle?) : BaseBottomSheetController(args),
    ViewModelHolder<VM> {

    private val viewModelHelper = ControllerViewModelHelper(this, this)

    init {
        addExternalLifecycleCallback(viewModelHelper)
    }

    override fun onDestroy() {
        removeExternalLifecycleCallback(viewModelHelper)
        super.onDestroy()
    }
}