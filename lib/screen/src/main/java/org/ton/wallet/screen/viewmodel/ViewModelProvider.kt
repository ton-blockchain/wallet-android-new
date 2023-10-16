package org.ton.wallet.screen.viewmodel

import android.view.View
import com.bluelinelabs.conductor.Controller

interface ViewModelProvider {

    fun <VM : BaseViewModel> get(key: String, factory: () -> VM): VM
}

class SimpleViewModelProvider(controller: Controller) : ViewModelProvider {

    private var viewModel: BaseViewModel? = null

    init {
        controller.addLifecycleListener(object : Controller.LifecycleListener() {
            override fun postCreateView(controller: Controller, view: View) {
                super.postCreateView(controller, view)
                viewModel?.onViewCreated()
            }
            override fun preDestroyView(controller: Controller, view: View) {
                viewModel?.onViewDestroyed()
                super.preDestroyView(controller, view)
            }
            override fun preDestroy(controller: Controller) {
                super.preDestroy(controller)
                viewModel?.onDestroy()
            }
        })
    }

    override fun <VM : BaseViewModel> get(key: String, factory: () -> VM): VM {
        val vm = factory.invoke()
        viewModel = vm
        return vm
    }
}

class CachedViewModelProvider : ViewModelProvider {

    private val map = HashMap<String, BaseViewModel>()

    @Suppress("UNCHECKED_CAST")
    override fun <VM : BaseViewModel> get(key: String, factory: () -> VM): VM {
        var viewModel = map[key]
        if (viewModel == null) {
            viewModel = factory.invoke()
            map[key] = viewModel
        }
        return viewModel as VM
    }

    fun <VM : BaseViewModel> remove(viewModel: VM) {
        viewModel.onDestroy()
        map.values.remove(viewModel)
    }
}
