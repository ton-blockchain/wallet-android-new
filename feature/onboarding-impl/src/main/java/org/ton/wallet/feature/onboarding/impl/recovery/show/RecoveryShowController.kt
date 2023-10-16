package org.ton.wallet.feature.onboarding.impl.recovery.show

import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.feature.onboarding.impl.base.ToolbarSlidingHeaderController
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.dialog.AlertDialog
import org.ton.wallet.uicomponents.view.AppToolbar
import org.ton.wallet.uicomponents.view.NumericTextView
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitFont
import kotlin.math.max

class RecoveryShowController(args: Bundle?) : BaseViewModelController<RecoveryShowViewModel>(args) {

    override val viewModel by viewModels { RecoveryShowViewModel(args.getScreenArguments()) }
    override val isSecured = true
    override val useTopInsetsPadding = false

    private lateinit var rootView: View
    private lateinit var toolbar: AppToolbar
    private lateinit var animationView: RLottieImageView

    private val doneButtonBottomMargin: Int
        get() = if (Res.isLandscapeScreenSize) Res.dp(20) else Res.dp(56)

    private var slidingHeaderController: ToolbarSlidingHeaderController? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        rootView = inflater.inflate(R.layout.screen_recovery_phrase, container, false)
        val scrollView = rootView.findViewById<NestedScrollView>(R.id.recoveryPhraseScrollView)

        val doneButton = rootView.findViewById<View>(R.id.recoveryPhraseDoneButton)
        doneButton.setOnClickListenerWithLock(viewModel::onDoneClicked)
        (doneButton.layoutParams as MarginLayoutParams).bottomMargin = doneButtonBottomMargin

        toolbar = rootView.findViewById(R.id.recoveryPhraseToolbar)
        toolbar.setTitleAlpha(0f)
        toolbar.setShadowAlpha(0f)

        animationView = rootView.findViewById(R.id.recoveryPhraseAnimationView)

        val titleText = rootView.findViewById<TextView>(R.id.recoveryPhraseTitle)
        slidingHeaderController = ToolbarSlidingHeaderController(toolbar, scrollView, animationView, titleText)
        scrollView.setOnScrollChangeListener(slidingHeaderController!!)
        return rootView
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.showAlertFlow.launchInViewScope(::showTimingDialog)
        viewModel.wordsFlow.launchInViewScope(::onWordsChanged)
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
        slidingHeaderController = null
        super.onDestroyView(view)
    }

    private fun onWordsChanged(words: List<String>) {
        val gridLayout = rootView.findViewById<GridLayout>(R.id.recoveryPhraseGridLayout)
        gridLayout.removeAllViews()
        var maxTextWidth = 0f
        words.forEachIndexed { index, text ->
            val textView = NumericTextView(context)
            textView.setTextColor(Res.color(RUiKitColor.text_primary))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            textView.text = text
            textView.typeface = Res.font(RUiKitFont.roboto_medium)
            textView.setNumber(index + 1)
            maxTextWidth = max(textView.textWidth, maxTextWidth)

            val isFirstColumn = index < 12
            val rowSpec = GridLayout.spec(index % 12, 1, 1f)
            val columnSpec = GridLayout.spec(index / 12, 1, 1f)
            val layoutParams = GridLayout.LayoutParams(rowSpec, columnSpec)
            layoutParams.setMargins(Res.dp(4))
            if (isFirstColumn) {
                layoutParams.updateMarginsRelative(end = Res.dp(64))
            }
            gridLayout.addView(textView, layoutParams)
        }
        gridLayout.forEach { child ->
            (child as NumericTextView).setMaxTextWidth(maxTextWidth)
        }
    }

    private fun showTimingDialog(withSkipButton: Boolean) {
        val negativeButton =
            if (withSkipButton) {
                Res.str(RString.skip) to DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                    viewModel.onSkipClicked()
                }
            } else {
                null
            }
        val dialog = AlertDialog.Builder(
            title = Res.str(RString.sure_done),
            message = Res.str(RString.you_didnt_have_time),
            positiveButton = Res.str(RString.ok_sorry) to DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() },
            negativeButton = negativeButton,
            isCancelable = false
        ).build(context)
        showDialog(dialog)
    }
}