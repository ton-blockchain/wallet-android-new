package org.ton.wallet.coreui

import android.view.View
import android.view.Window
import androidx.core.view.*

object KeyboardUtils {

    private val keyboardCallbackList = mutableListOf<(Boolean) -> Unit>()

    fun isKeyboardOpened(view: View): Boolean {
        val insets = ViewCompat.getRootWindowInsets(view) ?: return false
        return insets.isVisible(WindowInsetsCompat.Type.ime())
    }

    fun hideKeyboard(window: Window, clearFocus: Boolean = true, callback: (() -> Unit)? = null) {
        if (clearFocus) {
            window.currentFocus?.clearFocus()
        }
        if (isKeyboardOpened(window.decorView)) {
            callback?.let(::addKeyboardCallback)
            WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
        } else {
            callback?.invoke()
        }
    }

    fun showKeyboard(window: Window, callback: (() -> Unit)? = null) {
        callback?.let(::addKeyboardCallback)
        WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.ime())
    }

    fun onKeyboardClosed() {
        keyboardCallbackList.forEach { it.invoke(false) }
    }

    fun onKeyboardOpened() {
        keyboardCallbackList.forEach { it.invoke(true) }
    }

    fun onKeyboardAnimationFinished(view: View) {
        if (isKeyboardOpened(view)) {
            onKeyboardOpened()
        } else {
            onKeyboardClosed()
        }
    }

    private fun addKeyboardCallback(callback: () -> Unit) {
        val keyboardCallback = object : ((Boolean) -> Unit) {
            override fun invoke(isOpened: Boolean) {
                callback.invoke()
                keyboardCallbackList.remove(this)
            }
        }
        keyboardCallbackList.add(keyboardCallback)
    }
}