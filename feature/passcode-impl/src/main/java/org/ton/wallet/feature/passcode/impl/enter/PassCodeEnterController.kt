package org.ton.wallet.feature.passcode.impl.enter

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import org.ton.wallet.core.Res
import org.ton.wallet.feature.passcode.impl.base.BasePassCodeController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels

class PassCodeEnterController(args: Bundle?) : BasePassCodeController<PassCodeEnterViewModel>(args) {

    override val viewModel by viewModels { PassCodeEnterViewModel(args.getScreenArguments()) }

    override fun onPreCreateView() {
        super.onPreCreateView()
        viewModel.setActivity(activity!!)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        titleView.isVisible = false
        subTitleView.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = Res.dp(20) }
        optionsButton.isVisible = false
    }

    override fun onSetupOptionsClicked(v: View) {
        viewModel.showBiometricPrompt(activity as FragmentActivity)
    }
}