package org.ton.wallet.feature.onboarding.impl.finished

import android.graphics.Bitmap
import org.ton.wallet.feature.onboarding.api.OnboardingFinishedScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel

class OnboardingFinishedViewModel(
    args: OnboardingFinishedScreenArguments
) : BaseViewModel() {

    private val screenApi: OnboardingFinishedScreenApi by inject()

    val isImport: Boolean = args.isImport

    fun onDoneClicked(bitmap: Bitmap) {
        screenApi.navigateToMain(bitmap)
    }
}