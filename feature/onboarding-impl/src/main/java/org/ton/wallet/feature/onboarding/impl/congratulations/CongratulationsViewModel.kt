package org.ton.wallet.feature.onboarding.impl.congratulations

import org.ton.wallet.feature.onboarding.api.CongratulationsScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel

class CongratulationsViewModel : BaseViewModel() {

    private val screenApi: CongratulationsScreenApi by inject()

    fun onProceedClicked() {
        screenApi.navigateToShowRecovery()
    }
}