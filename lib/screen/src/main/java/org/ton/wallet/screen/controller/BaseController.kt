package org.ton.wallet.screen.controller

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import com.bluelinelabs.conductor.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.ton.wallet.di.injectScope
import org.ton.wallet.screen.controller.helper.*
import org.ton.wallet.screen.viewmodel.ViewModelDiScopeProvider
import pub.devrel.easypermissions.EasyPermissions

abstract class BaseController(args: Bundle?) : Controller(args),
    ControllerLifecycleHelper.LifecycleHelperCallback,
    OnApplyWindowInsetsListener,
    TargetResultHandler,
    PermissionsCallback {

    val controllerScope: CoroutineScope
        get() = scopeHelper.controllerScope

    private val barsHelper = ControllerBarsHelper(this)
    private val insetsHelper = ControllerInsetsHelper(this)
    private val lifecycleHelper = ControllerLifecycleHelper(this)
    private val permissionsHelper = ControllerPermissionsHelper()
    private val scopeHelper = ControllerCoroutineScopeHelper(this)
    private val secureHelper = ControllerSecureHelper(this)
    private val windowHelper = ControllerWindowHelper(this)

    protected open val isSecured: Boolean = false
    protected open val useTopInsetsPadding: Boolean = true
    protected open val useBottomInsetsPadding: Boolean = true
    protected open val useImeInsets: Boolean = true

    protected val context: Context
        get() = activity!!

    protected val lastInsets: WindowInsetsCompat
        get() = insetsHelper.lastInsets ?: WindowInsetsCompat.Builder().build()

    override fun onPreCreateView() {
        super.onPreCreateView()
        setStatusBarLight(true)
        setNavigationBarLight(true)
        permissionsHelper.addCallback(this)
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val contentView = createView(inflater, container, savedViewState)
        insetsHelper.useTopInsetsPadding = useTopInsetsPadding
        insetsHelper.useBottomInsetsPadding = useBottomInsetsPadding
        insetsHelper.useImeInsets = useImeInsets
        insetsHelper.onViewCreated(contentView)
        secureHelper.isSecured = isSecured
        return contentView
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        lifecycleHelper.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        lifecycleHelper.onActivityPaused(activity)
    }

    override fun onViewDestroy() {
        permissionsHelper.removeCallback(this)
        super.onViewDestroy()
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        return insetsHelper.onApplyWindowInsets(v, insets)
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        lifecycleHelper.onResultReceived(code, args)
    }

    override fun setTargetResult(code: String, args: Bundle?) {
        var resultHandler: TargetResultHandler? = targetController as? TargetResultHandler
        if (resultHandler == null) {
            resultHandler = activity as? TargetResultHandler
        }
        resultHandler?.onResultReceived(code, args)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, permissionsHelper)
    }

    open fun onActivityResultCallback(result: Any?) = Unit

    // --- ControllerBarsHelper
    protected fun setStatusBarLight(isLight: Boolean) {
        barsHelper.setStatusBarLight(isLight)
    }

    protected fun setNavigationBarLight(isLight: Boolean) {
        barsHelper.setNavigationBarLight(isLight)
    }

    // --- ControllerLifecycleHelper
    protected fun addExternalLifecycleCallback(callback: ControllerLifecycleHelper.ExternalLifecycleCallback) {
        lifecycleHelper.addExternalCallback(callback)
    }

    protected fun removeExternalLifecycleCallback(callback: ControllerLifecycleHelper.ExternalLifecycleCallback) {
        lifecycleHelper.removeExternalCallback(callback)
    }

    // --- ControllerPermissionsHelper
    internal fun addPermissionsCallback(callback: PermissionsCallback) {
        permissionsHelper.addCallback(callback)
    }

    internal fun removePermissionsCallback(callback: PermissionsCallback) {
        permissionsHelper.removeCallback(callback)
    }

    // --- ControllerWindowHelper
    protected fun showDialog(dialog: Dialog?) {
        windowHelper.showDialog(dialog)
    }

    protected fun showPopupWindow(popupWindow: PopupWindow?, anchorView: View) {
        windowHelper.showPopupWindow(popupWindow, anchorView)
    }

    // --- Coroutines
    protected fun <T> Flow<T>.launchInViewScope(action: suspend (T) -> Unit) {
        launchInScope(scopeHelper.viewScope, action)
    }

    protected fun <T> Flow<T>.launchInControllerScope(action: suspend (T) -> Unit) {
        launchInScope(scopeHelper.controllerScope, action)
    }

    private fun <T> Flow<T>.launchInScope(scope: CoroutineScope, action: suspend (T) -> Unit) {
        onEach { action.invoke(it) }.launchIn(scope)
    }

    // --- Di
    protected inline fun <reified T : Any> inject(): Lazy<T> {
        return injectScope(ViewModelDiScopeProvider.diScope)
    }
}