package org.ton.wallet.screen.controller.helper

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.*
import org.ton.wallet.screen.controller.BaseController

class ControllerLifecycleHelper(
    controller: BaseController
) : Controller.LifecycleListener() {

    private val internalCallback: LifecycleHelperCallback = controller
    private val externalCallbacks = mutableListOf<ExternalLifecycleCallback>()

    init {
        controller.addLifecycleListener(this)
    }

    override fun preCreateView(controller: Controller) {
        super.preCreateView(controller)
        internalCallback.onPreCreateView()
        externalCallbacks.forEach { it.onPreCreateView() }
    }

    override fun postCreateView(controller: Controller, view: View) {
        super.postCreateView(controller, view)
        internalCallback.onViewCreated(view)
        externalCallbacks.forEach { it.onViewCreated(view) }
    }

    override fun preDestroyView(controller: Controller, view: View) {
        internalCallback.onViewDestroy()
        externalCallbacks.forEach { it.onViewDestroy() }
        super.preDestroyView(controller, view)
    }

    override fun onChangeStart(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeStart(controller, changeHandler, changeType)
        externalCallbacks.forEach { it.onChangeStart(controller, changeHandler, changeType) }
    }

    override fun onChangeEnd(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnd(controller, changeHandler, changeType)
        externalCallbacks.forEach { it.onChangeEnd(controller, changeHandler, changeType) }
    }

    override fun preDetach(controller: Controller, view: View) {
        internalCallback.onPreDetach()
        externalCallbacks.forEach { it.onPreDetach() }
        super.preDetach(controller, view)
    }

    fun onActivityResumed(activity: Activity) {
        externalCallbacks.forEach { it.onActivityResumed(activity) }
    }

    fun onActivityPaused(activity: Activity) {
        externalCallbacks.forEach { it.onActivityPaused(activity) }
    }

    fun onResultReceived(code: String, args: Bundle?) {
        externalCallbacks.forEach { it.onResultReceived(code, args) }
    }

    fun addExternalCallback(callback: ExternalLifecycleCallback) {
        if (!externalCallbacks.contains(callback)) {
            externalCallbacks.add(callback)
        }
    }

    fun removeExternalCallback(callback: ExternalLifecycleCallback) {
        externalCallbacks.remove(callback)
    }


    interface LifecycleHelperCallback {

        fun onPreCreateView() = Unit

        fun onViewCreated(view: View) = Unit

        fun onViewDestroy() = Unit

        fun onPreDetach() = Unit
    }

    interface ExternalLifecycleCallback : LifecycleHelperCallback {

        fun onChangeStart(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) = Unit

        fun onChangeEnd(controller: Controller, changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) = Unit

        fun onActivityResumed(activity: Activity) = Unit

        fun onActivityPaused(activity: Activity) = Unit

        fun onResultReceived(code: String, args: Bundle?) = Unit
    }
}