package org.ton.wallet.screen.controller.helper

import android.app.Activity
import android.os.Bundle
import com.bluelinelabs.conductor.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ton.wallet.screen.controller.BaseController
import org.ton.wallet.screen.controller.ViewModelHolder
import org.ton.wallet.screen.viewmodel.BaseViewModel

class ControllerViewModelHelper<VM : BaseViewModel>(
    private val controller: BaseController,
    private val viewModelHolder: ViewModelHolder<VM>
) : ControllerLifecycleHelper.ExternalLifecycleCallback {

    override fun onPreCreateView() {
        super.onPreCreateView()
        val viewModel = viewModelHolder.viewModel
        controller.addPermissionsCallback(viewModel)
        viewModel.targetResultFlow
            .onEach { (code: String, args: Bundle?) ->
                controller.setTargetResult(code, args)
            }
            .launchIn(controller.controllerScope)
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        viewModelHolder.viewModel.onActivityResumed()
    }

    override fun onActivityPaused(activity: Activity) {
        viewModelHolder.viewModel.onActivityPaused()
        super.onActivityPaused(activity)
    }

    override fun onViewDestroy() {
        controller.removePermissionsCallback(viewModelHolder.viewModel)
        super.onViewDestroy()
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        viewModelHolder.viewModel.onResultReceived(code, args)
    }

    override fun onChangeStart(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeStart(controller, changeHandler, changeType)
        viewModelHolder.viewModel.onScreenChange(true, changeType.isPush, changeType.isEnter)
    }

    override fun onChangeEnd(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnd(controller, changeHandler, changeType)
        viewModelHolder.viewModel.onScreenChange(false, changeType.isPush, changeType.isEnter)
    }
}