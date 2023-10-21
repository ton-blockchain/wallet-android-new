package org.ton.wallet.feature.onboarding.impl.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.KeyboardUtils
import org.ton.wallet.coreui.ext.*
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.uicomponents.UiConst
import org.ton.wallet.uicomponents.view.AppEditText
import org.ton.wallet.uicomponents.view.NumericEditTextLayout

abstract class BaseInputListController<VM : BaseInputListViewModel>(args: Bundle?) : BaseViewModelController<VM>(args) {

    protected abstract val inputLayouts: Array<NumericEditTextLayout>

    private lateinit var suggestPopupWindow: SuggestWordsPopupWindow

    protected var currentFocusedEditText: EditText? = null

    override fun onPreCreateView() {
        super.onPreCreateView()
        suggestPopupWindow = SuggestWordsPopupWindow(context) { item, _ -> onWordSelected(item) }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.setOnClickListener(::dismissSuggestPopupWindow)
        viewModel.suggestWordsFlow.launchInViewScope(::onSuggestWordsChanged)
    }

    protected fun dismissSuggestPopupWindow() {
        if (suggestPopupWindow.isShowing) {
            suggestPopupWindow.dismiss()
        }
    }

    protected fun checkInputs(): Boolean {
        for (i in inputLayouts.indices) {
            val editText = inputLayouts[i].editText
            if (editText.length() == 0) {
                editText.clearFocus()
                editText.requestFocus()
                inputLayouts[i].animateShake(4, UiConst.ShakeAmplitude, UiConst.ShakeDurationMs)
                context.vibrate()
                return false
            }
        }
        return true
    }

    protected open fun onScrolled(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) = Unit

    private fun onSuggestWordsChanged(words: List<String>) {
        if (words.isEmpty()) {
            dismissSuggestPopupWindow()
        } else {
            suggestPopupWindow.setWords(words)
            if (!suggestPopupWindow.isShowing) {
                currentFocusedEditText?.let { suggestPopupWindow.show(it.parent as ViewGroup) }
            }
        }
    }

    protected open fun onWordSelected(word: String) {
        currentFocusedEditText?.let { et ->
            et.setTextWithSelection(word)
            val editTextPosition = inputLayouts.indexOfFirst { it.editText == et }
            if (editTextPosition != -1) {
                if (editTextPosition < inputLayouts.size - 1) {
                    inputLayouts[editTextPosition + 1].editText.requestFocus()
                } else {
                    KeyboardUtils.hideKeyboard(activity!!.window, clearFocus = false)
                }
            }
        }
        dismissSuggestPopupWindow()
    }

    protected val editTextFocusChangedListener = object : NumericEditTextLayout.TextFocusChangedListener {

        override fun onTextFocusChanged(v: AppEditText, text: CharSequence, isFocused: Boolean) {
            val editTextLayout = v.parent as NumericEditTextLayout
            if (isFocused) {
                currentFocusedEditText = v
                val position = inputLayouts.indexOf(editTextLayout)
                viewModel.setEnteredWord(position, text.toString().trim())
                if (text.isEmpty()) {
                    dismissSuggestPopupWindow()
                } else if (!suggestPopupWindow.isShowing
                    && !Res.isLandscapeScreenSize
                    && viewModel.suggestWordsFlow.value.isNotEmpty()
                    && viewModel.suggestWordsFlow.value.first() != text
                ) {
                    suggestPopupWindow.show(editTextLayout)
                }
            } else {
                dismissSuggestPopupWindow()
            }
        }
    }

    protected val scrollChangeListener = View.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
        dismissSuggestPopupWindow()
        onScrolled(v, scrollX, scrollY, oldScrollX, oldScrollY)
    }
}