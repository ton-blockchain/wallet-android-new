package org.ton.wallet.feature.onboarding.impl.recovery.check

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.KeyboardUtils
import org.ton.wallet.coreui.ext.*
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.feature.onboarding.impl.base.BaseInputListController
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.dialog.AlertDialog
import org.ton.wallet.uicomponents.view.AppToolbar
import org.ton.wallet.uicomponents.view.NumericEditTextLayout
import kotlin.math.max

class RecoveryCheckController(args: Bundle?) : BaseInputListController<RecoveryCheckViewModel>(args) {

    override val viewModel by viewModels { RecoveryCheckViewModel() }

    override val inputLayouts: Array<NumericEditTextLayout> get() = editTextLayouts

    private lateinit var animationView: RLottieImageView
    private lateinit var appToolbar: AppToolbar
    private lateinit var continueButton: TextView
    private lateinit var editTextLayouts: Array<NumericEditTextLayout>

    private val continueButtonBottomMargin: Int
        get() = if (Res.isLandscapeScreenSize) Res.dp(20) else Res.dp(100)

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_recovery_check, container, false)
        view.findViewById<NestedScrollView>(R.id.recoveryCheckScrollView).setOnScrollChangeListener(scrollChangeListener)
        view.findViewById<View>(R.id.recoveryCheckLinearLayout).setOnClickListener(::dismissSuggestPopupWindow)

        animationView = view.findViewById(R.id.recoveryCheckAnimationView)

        appToolbar = view.findViewById(R.id.recoveryCheckToolbar)
        appToolbar.setTitleAlpha(0f)
        appToolbar.setShadowAlpha(0f)

        continueButton = view.findViewById(R.id.recoveryCheckContinueButton)
        (continueButton.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = continueButtonBottomMargin
        continueButton.setOnClickListenerWithLock(::onDoneClicked)

        // setup subtitle
        val subtitleText = view.findViewById<TextView>(R.id.recoveryCheckSubTitle)
        subtitleText.text = viewModel.subtitle

        // setup inputs
        var maxNumberWidth = 0f
        editTextLayouts = arrayOf(
            view.findViewById(R.id.recoveryCheckEditText1),
            view.findViewById(R.id.recoveryCheckEditText2),
            view.findViewById(R.id.recoveryCheckEditText3),
        )
        for (i in editTextLayouts.indices) {
            val editTextLayout = editTextLayouts[i]
            editTextLayout.editText.setOnEditorActionListener(editorActionListener)
            editTextLayout.editText.setTextWithSelection(viewModel.enteredWords[i])
            editTextLayout.setNumber(viewModel.wordPositions[i] + 1)
            editTextLayout.setTextFocusChangedListener(editTextFocusChangedListener)
            maxNumberWidth = max(editTextLayouts[i].numberTextWidth, maxNumberWidth)
        }
        for (i in editTextLayouts.indices) {
            editTextLayouts[i].setMaxTextWidth(maxNumberWidth)
        }

        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.errorStatesFlow.launchInViewScope(::onErrorStatesChanged)
        viewModel.showErrorDialog.launchInViewScope(::showErrorDialog)
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val superInsets = super.onApplyWindowInsets(v, insets)
        (continueButton.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
            if (KeyboardUtils.isKeyboardOpened(v)) Res.dp(20)
            else continueButtonBottomMargin
        continueButton.requestLayout()
        return superInsets
    }

    override fun onDoneClicked() {
        super.onDoneClicked()
        dismissSuggestPopupWindow()
        if (checkInputs()) {
            viewModel.onContinueClicked(activity!!)
        }
    }

    private fun onErrorStatesChanged(errors: BooleanArray) {
        editTextLayouts.forEachIndexed { index, numericEditTextLayout ->
            numericEditTextLayout.editText.setErrorState(errors[index])
        }
    }

    private fun showErrorDialog(value: Unit) {
        val alertDialog = AlertDialog.Builder(
            title = Res.str(RString.incorrect_words),
            message = Res.str(RString.incorrect_words_description),
            positiveButton = Res.str(RString.try_again) to DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            },
            negativeButton = Res.str(RString.see_words) to DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
                viewModel.onSeeWordsClicked()
            }
        ).build(context)
        showDialog(alertDialog)
    }
}