package org.ton.wallet.feature.onboarding.api

import android.graphics.Bitmap

interface OnboardingFinishedScreenApi {

    fun navigateToMain(bitmap: Bitmap)
}