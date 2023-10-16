package org.ton.wallet.feature.passcode.impl.setup

import android.os.Bundle
import android.view.View
import org.ton.wallet.feature.passcode.impl.base.BasePassCodeController
import org.ton.wallet.screen.getScreenArguments
import org.ton.wallet.screen.viewmodel.viewModels

class PassCodeSetupController(args: Bundle?) : BasePassCodeController<PassCodeSetupViewModel>(args) {

    override val viewModel by viewModels { PassCodeSetupViewModel(args.getScreenArguments()) }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setStatusBarLight(true)
        setNavigationBarLight(true)
    }
}