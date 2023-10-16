package org.ton.wallet.app.screen

import android.graphics.Bitmap
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.onboarding.api.OnboardingFinishedScreenApi
import org.ton.wallet.feature.wallet.impl.main.MainScreenController
import org.ton.wallet.screen.AppScreen

internal class OnboardingFinishedScreenApiImpl(
    private val navigator: Navigator
) : OnboardingFinishedScreenApi {

    override fun navigateToMain(bitmap: Bitmap) {
        MainScreenController.BitmapForAnimation = bitmap
        navigator.push(
            screen = AppScreen.Main.name,
            isReplace = true,
            isRoot = true,
            isAnimated = false
        )
    }
}