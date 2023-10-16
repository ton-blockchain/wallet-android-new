package org.ton.wallet.feature.onboarding.impl.recovery.finished

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.lib.security.BiometricUtils
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.view.CheckBoxTextView

class RecoveryFinishedController(args: Bundle?) : BaseViewModelController<RecoveryFinishedViewModel>(args) {

    override val viewModel by viewModels { RecoveryFinishedViewModel(args.getScreenArguments()) }

    private lateinit var animationView: RLottieImageView
    private lateinit var checkBoxView: CheckBoxTextView

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_recovery_checked, container, false)
        view.findViewById<View>(R.id.recoveryCheckedDoneButton).setOnClickListenerWithLock {
            viewModel.onDoneClicked(activity!!)
        }
        animationView = view.findViewById(R.id.recoveryCheckedAnimationView)
        checkBoxView = view.findViewById(R.id.recoveryCheckedBiometricsCheckBox)
        checkBoxView.setCheckChangedListener(viewModel::onBiometricCheckChanged)
        checkBoxView.isVisible = BiometricUtils.isBiometricsAvailableOnDevice(context)
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.isBiometricCheckedFlow.launchInViewScope(checkBoxView::setChecked)
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
        }
    }
}