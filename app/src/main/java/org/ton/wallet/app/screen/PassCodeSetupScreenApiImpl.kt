package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.passcode.api.PassCodeSetupScreenApi
import org.ton.wallet.feature.passcode.impl.setup.PassCodeSetupScreenArguments

internal class PassCodeSetupScreenApiImpl(
    private val navigator: Navigator
) : PassCodeSetupScreenApi {

    override fun navigateToPassCodeSetupCheck(passCode: String, withBiometrics: Boolean) {
        val arguments = PassCodeSetupScreenArguments(
            isBackVisible = true,
            isDark = false,
            passCode = passCode,
            withBiometrics = withBiometrics
        )
        navigator.push(arguments)
    }

    override fun navigateBack() {
        navigator.pop(false)
    }
}