package org.ton.wallet.screen.viewmodel

import android.os.Bundle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ton.wallet.di.injectScope
import org.ton.wallet.screen.controller.PermissionsCallback
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : PermissionsCallback {

    protected val viewModelScope: CoroutineScope

    private val _targetResultFlow = MutableSharedFlow<Pair<String, Bundle?>>(replay = 1)
    val targetResultFlow: Flow<Pair<String, Bundle?>> = _targetResultFlow

    init {
        var coroutineContext: CoroutineContext = SupervisorJob()
        DefaultCoroutineExceptionHandler?.let { coroutineContext += it }
        viewModelScope = CoroutineScope(coroutineContext)
    }

    open fun onViewCreated() = Unit

    open fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) = Unit

    open fun onResultReceived(code: String, args: Bundle?) = Unit

    open fun onActivityResumed() = Unit

    open fun onActivityPaused() = Unit

    open fun onViewDestroyed() = Unit

    open fun onDestroy() {
        viewModelScope.cancel()
    }

    protected fun getErrorMessage(error: Throwable): String? {
        return ErrorMessageProvider?.invoke(error)
    }

    protected fun setResult(code: String, args: Bundle? = null) {
        _targetResultFlow.tryEmit(code to args)
    }

    protected inline fun <reified T : Any> inject(name: String? = null): Lazy<T> {
        return injectScope(ViewModelDiScopeProvider.diScope, name)
    }

    companion object {

        var DefaultCoroutineExceptionHandler: CoroutineExceptionHandler? = null
        var ErrorMessageProvider: ((Throwable) -> String?)? = null
    }
}