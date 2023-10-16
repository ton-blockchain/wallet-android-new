package org.ton.wallet.feature.onboarding.impl.start

import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import org.ton.wallet.coreui.ext.setOnClickListener
import org.ton.wallet.feature.onboarding.impl.R
import org.ton.wallet.rlottie.RLottieImageView
import org.ton.wallet.screen.controller.BaseViewModelController
import org.ton.wallet.screen.viewmodel.viewModels
import org.ton.wallet.uicomponents.dialog.IndeterminateProgressDialog

class StartController(args: Bundle?) : BaseViewModelController<StartViewModel>(args) {

    override val viewModel by viewModels { StartViewModel() }

    private lateinit var animationView: RLottieImageView

    private var progressDialog: IndeterminateProgressDialog? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.screen_onboarding_start, container, false)
        view.findViewById<View>(R.id.startCreateButton).setOnClickListener(viewModel::onCreateClicked)
        view.findViewById<View>(R.id.startImportButton).setOnClickListener(viewModel::onImportClicked)

        animationView = view.findViewById(R.id.startAnimationView)

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
            showDialog(progressDialog!!)
        } else {
            progressDialog?.dismiss()
        }
    }
}