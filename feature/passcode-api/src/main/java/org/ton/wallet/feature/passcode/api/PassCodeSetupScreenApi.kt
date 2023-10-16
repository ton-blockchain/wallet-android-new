package org.ton.wallet.feature.passcode.api

interface PassCodeSetupScreenApi : BasePasscodeScreenApi {

    fun navigateToPassCodeSetupCheck(passCode: String, withBiometrics: Boolean)

    companion object {

        const val ResultCodePassCodeSet = "passCodeSet"
    }
}