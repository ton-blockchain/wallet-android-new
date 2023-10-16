package org.ton.wallet.feature.send.impl.amount

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.ext.*
import org.ton.wallet.feature.send.impl.R
import org.ton.wallet.screen.controller.BaseViewModelBottomSheetController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.BottomSheetHelper
import org.ton.wallet.uicomponents.view.NumPadView
import org.ton.wallet.uicomponents.view.SwitchView
import org.ton.wallet.uicomponents.view.amount.AmountEditText
import org.ton.wallet.uikit.RUiKitColor

class SendAmountController(args: Bundle?) : BaseViewModelBottomSheetController<SendAmountViewModel>(args) {

    override val viewModel by viewModels { SendAmountViewModel(args.getScreenArguments()) }
    override val isFullHeight = true

    private lateinit var amountEditText: AmountEditText
    private lateinit var sendAllAmountText: TextView
    private lateinit var sendAllAmountSwitch: SwitchView
    private lateinit var insufficientFundsText: TextView
    private lateinit var continueButton: View

    override fun createBottomSheetView(inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_send_amount, container, false)
        BottomSheetHelper.connectAppToolbarWithScrollableView(view.findViewById(R.id.sendAmountToolbar), view.findViewById(R.id.sendAmountScrollView))
        view.findViewById<TextView>(R.id.sendAmountToText).text = viewModel.sendToString
        view.findViewById<View>(R.id.sendAmountSendAllBackground).setOnClickListener(viewModel::onSendAllClicked)
        view.findViewById<NumPadView>(R.id.sendAmountNumPadView).setNumPadViewListener(viewModel)

        continueButton = view.findViewById(R.id.sendAmountContinueButton)
        continueButton.setOnClickListenerWithLock(viewModel::onContinueClicked)

        view.findViewById<TextView>(R.id.sendAmountToEdit).isVisible = viewModel.isAddressEditable
        if (viewModel.isAddressEditable) {
            view.findViewById<View>(R.id.sendAmountToBackground).setOnClickListener(viewModel::onEditAddressClicked)
        }

        amountEditText = view.findViewById(R.id.sendAmountEditText)
        amountEditText.addSelectionChangedListener(viewModel::onTextSelectionChanged)
        amountEditText.doOnLayout { v ->
            insufficientFundsText.translationY = v.top.toFloat() + v.height - v.paddingBottom + Res.dp(4)
        }

        insufficientFundsText = view.findViewById(R.id.sendAmountInsufficientFundsText)
        sendAllAmountText = view.findViewById(R.id.sendAmountSendAllValue)
        sendAllAmountSwitch = view.findViewById(R.id.sendAmountSendAllSwitch)

        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.balanceFlow.launchInViewScope(::onBalanceChanged)
        viewModel.amountFlow.launchInViewScope(amountEditText::setText)
        viewModel.amountSelectionFlow.launchInViewScope(amountEditText::setSelectionSafe)
        viewModel.sendAllCheckedFlow.launchInViewScope { isChecked -> sendAllAmountSwitch.setChecked(isChecked, true) }
        viewModel.isInsufficientFundsFlow.launchInViewScope(::onInsufficientFundsChanged)
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeStarted(changeHandler, changeType)
        if (changeType.isPush && !changeType.isEnter) {
            activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            amountEditText.clearFocus()
        }
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            amountEditText.requestFocus()
        }
    }

    override fun setBottomSheetTranslation(translation: Float) {
        super.setBottomSheetTranslation(translation)
        continueButton.invalidate()
    }

    private fun onBalanceChanged(balance: Long?) {
        sendAllAmountText.text = balance?.let(Formatter::getFormattedAmount)
        sendAllAmountText.isVisible = balance != null
    }

    private fun onInsufficientFundsChanged(isInsufficientFunds: Boolean) {
        insufficientFundsText.isVisible = isInsufficientFunds
        amountEditText.setTextColor(Res.color(if (isInsufficientFunds) RUiKitColor.text_error else RUiKitColor.text_primary))
    }
}