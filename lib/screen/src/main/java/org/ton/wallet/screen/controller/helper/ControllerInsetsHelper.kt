package org.ton.wallet.screen.controller.helper

import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.util.forEach
import androidx.core.view.*
import org.ton.wallet.coreui.KeyboardUtils
import org.ton.wallet.screen.controller.BaseController

internal class ControllerInsetsHelper(
    private val controller: BaseController
) : OnApplyWindowInsetsListener {

    var useTopInsetsPadding: Boolean = true
    var useBottomInsetsPadding: Boolean = true
    var useImeInsets: Boolean = true

    var lastInsets: WindowInsetsCompat? = null
        private set

    fun onViewCreated(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view, controller)
        ViewCompat.setWindowInsetsAnimationCallback(view, insetsAnimationCallback)
        view.doOnAttach(ViewCompat::requestApplyInsets)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        lastInsets = insets

        val newInsetsMap = SparseArray<Rect>()
        InsetsTypes.forEach { insetsType ->
            newInsetsMap.put(insetsType, getRect(insets.getInsets(insetsType)))
        }

        val filteredInsets = WindowInsetsCompat.Builder(insets)
            .setInsets(WindowInsetsCompat.Type.systemBars(), insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars()))
            .build()

        var insetsTypeMask = WindowInsetsCompat.Type.systemBars()
        if (useImeInsets) {
            insetsTypeMask += WindowInsetsCompat.Type.ime()
        }
        val typedInsets = filteredInsets.getInsets(insetsTypeMask)

        if (useTopInsetsPadding) {
            v.updatePadding(top = typedInsets.top)
            newInsetsMap[WindowInsetsCompat.Type.systemBars()].top = 0
        }
        if (useBottomInsetsPadding) {
            v.updatePadding(bottom = typedInsets.bottom)
            newInsetsMap[WindowInsetsCompat.Type.systemBars()].bottom = 0
        }
        v.updatePadding(left = typedInsets.left, right = typedInsets.right)

        val newInsetsBuilder = WindowInsetsCompat.Builder()
        newInsetsMap.forEach { insetsType, insetsRect ->
            newInsetsBuilder.setInsets(insetsType, Insets.of(insetsRect))
        }

        // return old insets because old Android version not supported property behaviour
        return insets
    }

    private val insetsAnimationCallback = object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

        override fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
            return insets
        }

        override fun onEnd(animation: WindowInsetsAnimationCompat) {
            super.onEnd(animation)
            if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
                KeyboardUtils.onKeyboardAnimationFinished(controller.activity!!.window.decorView)
            }
        }
    }

    private companion object {

        private val InsetsTypes = intArrayOf(
            WindowInsetsCompat.Type.ime(),
            WindowInsetsCompat.Type.systemBars(),
            WindowInsetsCompat.Type.systemGestures(),
            WindowInsetsCompat.Type.mandatorySystemGestures(),
            WindowInsetsCompat.Type.tappableElement()
        )

        private fun getRect(insets: Insets): Rect {
            return Rect(insets.left, insets.top, insets.right, insets.bottom)
        }
    }
}