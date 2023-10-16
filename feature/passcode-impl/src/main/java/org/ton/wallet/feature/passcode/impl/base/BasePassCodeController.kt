package org.ton.wallet.feature.passcode.impl.base

import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.*
import org.ton.wallet.feature.passcode.impl.R
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.UiConst
import org.ton.wallet.uicomponents.popup.MenuPopupWindow
import org.ton.wallet.uicomponents.view.NumPadView
import org.ton.wallet.uicomponents.view.PassCodeView
import org.ton.wallet.uikit.RUiKitColor

abstract class BasePassCodeController<VM : BasePassCodeViewModel>(args: Bundle?) : BaseViewModelController<VM>(args) {

    private lateinit var animationView: RLottieImageView
    private lateinit var passCodeView: PassCodeView
    protected lateinit var titleView: TextView
        private set
    protected lateinit var subTitleView: TextView
        private set
    protected lateinit var optionsButton: TextView
        private set

    private var isFirstStateSet = true
    private var isPassCodeAnimating = false
    private var popupWindow: PopupWindow? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_passcode, container, false)
        val dark = viewModel.isDark
        view.setBackgroundColor(Res.color(
            if (dark) RUiKitColor.common_black
            else RUiKitColor.common_white
        ))
        setStatusBarLight(!dark)
        setNavigationBarLight(!dark)

        val backButton = view.findViewById<View>(R.id.passCodeBackButton)
        backButton.setOnClickListenerWithLock(viewModel::onBackClicked)
        backButton.isVisible = viewModel.isBackVisible

        val numPadView = view.findViewById<NumPadView>(R.id.passCodeNumPadView)
        numPadView.setDark(dark)
        numPadView.setNumPadViewListener(numPadViewListener)

        animationView = view.findViewById(R.id.passCodeAnimationView)

        optionsButton = view.findViewById(R.id.passCodeOptionsText)
        optionsButton.setOnClickListener(::onSetupOptionsClicked)

        passCodeView = view.findViewById(R.id.passCodePassCodeView)
        passCodeView.setDark(dark)

        val textColor = Res.color(if (dark) RUiKitColor.common_white else RUiKitColor.common_black)
        titleView = view.findViewById(R.id.passCodeTitle)
        titleView.setTextColor(textColor)
        subTitleView = view.findViewById(R.id.passCodeSubTitle)
        subTitleView.setTextColor(textColor)

        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        isFirstStateSet = true
        viewModel.screenStateFlow.launchInViewScope(::onStateChanged)
        viewModel.errorEventFlow.launchInViewScope(::onError)
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
        }
    }

    override fun onDestroyView(view: View) {
        passCodeView.animate().cancel()
        super.onDestroyView(view)
    }

    private fun onStateChanged(state: PassCodeScreenState) {
        titleView.text = state.title
        titleView.isVisible = !state.title.isNullOrEmpty()
        subTitleView.text = state.subtitle
        subTitleView.updateLayoutParams<MarginLayoutParams> {
            topMargin = if (titleView.isVisible) Res.dp(12) else Res.dp(20)
        }
        passCodeView.setDotsCount(state.passCodeLength)
        passCodeView.setFilledDots(state.filledDotsCount, !isFirstStateSet)
        optionsButton.text = state.optionsText
        optionsButton.isVisible = state.optionsText != null
        isFirstStateSet = false
        setPassCodeAnimating(state.isLoading)
    }

    private fun onError(unit: Unit) {
        context.vibrate()
        passCodeView.animateShake(4, UiConst.ShakeAmplitude, UiConst.ShakeDurationMs, scaleToOriginal = true)
    }

    private fun setPassCodeAnimating(isAnimating: Boolean) {
        if (isPassCodeAnimating == isAnimating) {
            return
        }
        passCodeView.animate().cancel()
        if (isAnimating) {
            val duration = 10000L
            val count = duration.toInt() / 1000
            passCodeView.animateBounce(count, UiConst.BounceAmplitude, durationMs = duration)
        } else {
            passCodeView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200L)
                .start()
        }
    }

    protected open fun onSetupOptionsClicked(v: View) {
        if (popupWindow != null) {
            popupWindow = null
            return
        }
        val items = listOf(
            MenuPopupWindow.MenuPopupItem(Res.str(RString.passcode_for_digit), viewModel::onForDigitPassCodeClicked),
            MenuPopupWindow.MenuPopupItem(Res.str(RString.passcode_six_digit), viewModel::onSixDigitPassCodeClicked),
        )
        popupWindow = MenuPopupWindow(v.context)
            .setItems(items)
            .setMinimumWidth(Res.dp(220))
        popupWindow?.setOnDismissListener { popupWindow = null }
        showPopupWindow(popupWindow, v)
    }

    private val numPadViewListener = object : NumPadView.NumPadViewListener {
        override fun onNumberClicked(number: Int) = viewModel.onNumberEntered(number.toString())
        override fun onBackSpaceClicked() = viewModel.onBackSpaceClicked()
        override fun onBackSpaceLongClicked() = viewModel.onClearClicked()
    }
}