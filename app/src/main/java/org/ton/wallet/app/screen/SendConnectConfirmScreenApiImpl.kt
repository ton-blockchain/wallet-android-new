package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.navigation.changehandler.SlideChangeHandler
import org.ton.wallet.feature.passcode.impl.enter.PassCodeEnterScreenArguments
import org.ton.wallet.feature.send.api.SendConnectConfirmScreenApi

class SendConnectConfirmScreenApiImpl(
    private val navigator: Navigator
) : SendConnectConfirmScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToPassCodeEnter(purpose: String) {
        navigator.push(
            arguments = PassCodeEnterScreenArguments(
                purpose = purpose,
                isBackVisible = false,
                isDark = false,
                isOnlyPassCode = false
            ),
            pushChangeHandler = SlideChangeHandler.create(false),
            popChangeHandler = SlideChangeHandler.create(false)
        )
    }
}