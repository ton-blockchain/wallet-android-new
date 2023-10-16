package org.ton.wallet.screen.viewmodel

import android.app.Activity
import com.bluelinelabs.conductor.Controller

@PublishedApi
internal val appViewModelProvider by lazy {
    CachedViewModelProvider()
}

enum class ViewModelRetainType {
    Application,
    Screen
}

class ViewModelLazy<VM : BaseViewModel>(
    private val key: String,
    private val factory: () -> VM,
    private val provider: ViewModelProvider
) : Lazy<VM> {

    private var _value: VM? = null

    override val value: VM
        get() {
            if (_value == null) {
                _value = provider.get(key, factory)
            }
            return _value!!
        }

    override fun isInitialized(): Boolean {
        return _value != null
    }
}

inline fun <reified VM : BaseViewModel> Activity.appViewModels(
    noinline factory: () -> VM
): Lazy<VM> {
    return ViewModelLazy(
        key = VM::class.java.canonicalName!!,
        factory = factory,
        provider = appViewModelProvider
    )
}

inline fun <reified VM : BaseViewModel> Controller.appViewModels(
    noinline factory: () -> VM
): Lazy<VM> {
    return viewModels(ViewModelRetainType.Application, factory)
}

inline fun <reified VM : BaseViewModel> Controller.viewModels(
    noinline factory: () -> VM
): Lazy<VM> {
    return viewModels(ViewModelRetainType.Screen, factory)
}

inline fun <reified VM : BaseViewModel> Controller.viewModels(
    retainType: ViewModelRetainType,
    noinline factory: () -> VM
): Lazy<VM> {
    val viewModelProvider = when (retainType) {
        ViewModelRetainType.Application -> appViewModelProvider
        ViewModelRetainType.Screen -> SimpleViewModelProvider(this)
    }
    return ViewModelLazy(
        key = VM::class.java.canonicalName!!,
        factory = factory,
        provider = viewModelProvider
    )
}
