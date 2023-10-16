package org.ton.wallet.feature.onboarding.impl.nophrase

import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.coreui.ext.setOnClickListenerWithLock
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.dialog.IndeterminateProgressDialog

class NoPhraseController(args: Bundle?) : BaseViewModelController<NoPhraseViewModel>(args) {

    override val viewModel by viewModels { NoPhraseViewModel() }

    private lateinit var animationView: RLottieImageView

    private var progressDialog: IndeterminateProgressDialog? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_no_phrase, container, false)
        view.findViewById<View>(R.id.noPhraseBackButton).setOnClickListenerWithLock(viewModel::onBackClicked)
        view.findViewById<View>(R.id.noPhraseDoneButton).setOnClickListenerWithLock(viewModel::onEnterClicked)
        view.findViewById<View>(R.id.noPhraseCreateButton).setOnClickListenerWithLock(viewModel::onCreateClicked)
        animationView = view.findViewById(R.id.noPhraseAnimationView)
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        viewModel.isLoadingFlow.launchInViewScope(::onLoadingChanged)
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (changeType.isEnter) {
            animationView.playAnimation()
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
}