package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.send.api.SendProcessingScreenApi
import org.ton.wallet.screen.AppScreen

class SendProcessingScreenApiImpl(
    private val navigator: Navigator
) : SendProcessingScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToMain() {
        navigator.popTo(AppScreen.Main.name, true)
    }
}