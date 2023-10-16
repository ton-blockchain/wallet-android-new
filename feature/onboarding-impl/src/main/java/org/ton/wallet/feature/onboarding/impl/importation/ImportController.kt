package org.ton.wallet.feature.onboarding.impl.importation

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.*
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.feature.onboarding.impl.base.BaseInputListController
import org.ton.wallet.feature.onboarding.impl.base.ToolbarSlidingHeaderController
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.dialog.AlertDialog
import org.ton.wallet.uicomponents.dialog.IndeterminateProgressDialog
import org.ton.wallet.uicomponents.view.AppToolbar
import org.ton.wallet.uicomponents.view.NumericEditTextLayout
import org.ton.wallet.uikit.RUiKitDimen
import kotlin.math.max

class ImportController(args: Bundle?) : BaseInputListController<ImportViewModel>(args) {

    override val viewModel by viewModels { ImportViewModel() }
    override val useTopInsetsPadding: Boolean = false

    override val inputLayouts: Array<NumericEditTextLayout>
        get() = editTextLayouts as Array<NumericEditTextLayout>

    private val editTextWidth = Res.dimenInt(RUiKitDimen.phrase_word_width)

    private lateinit var animationView: RLottieImageView
    private lateinit var contentLayout: LinearLayout
    private lateinit var editTextLayouts: Array<NumericEditTextLayout?>

    private var slidingHeaderController: ToolbarSlidingHeaderController? = null
    private var progressDialog: IndeterminateProgressDialog? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_import, container, false)

        val scrollView = view.findViewById<NestedScrollView>(R.id.importScrollView)
        scrollView.setOnScrollChangeListener(scrollChangeListener)
        view.findViewById<View>(R.id.importDontHaveButton).setOnClickListenerWithLock(viewModel::onNoPhraseClicked)

        val doneButton = view.findViewById<View>(R.id.importDoneButton)
        doneButton.setOnClickListenerWithLock(::onDoneClicked)

        contentLayout = view.findViewById(R.id.importContentLayout)
        contentLayout.setOnClickListener(::dismissSuggestPopupWindow)

        // setup inputs
        val childPosition = contentLayout.indexOfChild(doneButton)
        var maxNumberWidth = 0f
        val recoveryWordsCount = viewModel.enteredWords.size
        editTextLayouts = Array(recoveryWordsCount) { null }
        for (i in 0 until recoveryWordsCount) {
            val editTextLayout = PreCreatedEditTextLayouts?.get(i) ?: NumericEditTextLayout(context)
            editTextLayout.editText.setTextWithSelection(viewModel.enteredWords[i])
            editTextLayout.setNumber(i + 1)
            editTextLayout.setTextFocusChangedListener(editTextFocusChangedListener)

            val layoutParams = LinearLayout.LayoutParams(editTextWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.topMargin = if (i == 0) Res.dp(20) else Res.dp(8)
            contentLayout.addView(editTextLayout, childPosition + i, layoutParams)

            maxNumberWidth = max(maxNumberWidth, editTextLayout.numberTextWidth)
            editTextLayouts[i] = editTextLayout
        }
        for (i in editTextLayouts.indices) {
            editTextLayouts[i]?.setMaxTextWidth(maxNumberWidth)
        }

        animationView = view.findViewById(R.id.importAnimationView)

        val importTitleText = view.findViewById<TextView>(R.id.importTitleText)
        val appToolbar = view.findViewById<AppToolbar>(R.id.importToolbar)
        slidingHeaderController = ToolbarSlidingHeaderController(appToolbar, scrollView, animationView, importTitleText)

        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.isLoadingFlow.launchInViewScope(::onLoadingChanged)
        viewModel.showIncorrectWordsDialog.launchInViewScope(::showIncorrectWordsDialog)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val superInsets = super.onApplyWindowInsets(v, insets)
        slidingHeaderController?.onInsetsChanged(superInsets)
        return superInsets
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
        }
    }

    override fun onDestroyView(view: View) {
        PreCreatedEditTextLayouts?.forEach { (it.parent as? ViewGroup)?.removeView(it) }
        slidingHeaderController = null
        super.onDestroyView(view)
    }

    override fun onScrolled(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrolled(v, scrollX, scrollY, oldScrollX, oldScrollY)
        slidingHeaderController?.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY)
    }

    private fun onDoneClicked() {
        if (checkInputs()) {
            viewModel.onDoneClicked()
        }
    }

    private fun onLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            progressDialog = IndeterminateProgressDialog(context, false)
            showDialog(progressDialog)
        } else {
            progressDialog?.dismiss()
        }
    }

    private fun showIncorrectWordsDialog(value: Unit) {
        val alertDialog = AlertDialog.Builder(
            title = Res.str(RString.incorrect_words),
            message = Res.str(RString.incorrect_secret_words),
            positiveButton = Res.str(RString.ok) to DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() },
        ).build(context)
        showDialog(alertDialog)
    }

    companion object {

        var PreCreatedEditTextLayouts: Array<NumericEditTextLayout>? = null
    }
}