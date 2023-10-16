package org.ton.wallet.feature.onboarding.impl.congratulations

import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.viewmodel.viewModels

class CongratulationsController(args: Bundle?) : BaseViewModelController<CongratulationsViewModel>(args) {

    override val viewModel by viewModels { CongratulationsViewModel() }

    private lateinit var animationView: RLottieImageView

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_congratulations, container, false)
        view.findViewById<View>(R.id.passCodeCompletedDoneButton).setOnClickListenerWithLock(viewModel::onProceedClicked)
        animationView = view.findViewById(R.id.congratulationsAnimationView)
        return view
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
        }
    }
}