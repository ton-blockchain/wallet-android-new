package org.ton.wallet.screen.controller.helper

import android.view.View
import com.bluelinelabs.conductor.Controller
import kotlinx.coroutines.*
import org.ton.wallet.screen.controller.BaseController

internal class ControllerCoroutineScopeHelper(controller: BaseController) : Controller.LifecycleListener() {

    val controllerScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    lateinit var viewScope: CoroutineScope
        private set

    init {
        controller.addLifecycleListener(this)
    }

    override fun preCreateView(controller: Controller) {
        super.preCreateView(controller)
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    override fun preDestroyView(controller: Controller, view: View) {
        viewScope.cancel()
        super.preDestroyView(controller, view)
    }

    override fun preDestroy(controller: Controller) {
        controllerScope.cancel()
        super.postDestroy(controller)
    }
}