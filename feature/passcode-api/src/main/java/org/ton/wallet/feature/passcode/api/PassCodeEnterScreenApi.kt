package org.ton.wallet.feature.passcode.api

import android.app.Activity

interface PassCodeEnterScreenApi : BasePasscodeScreenApi {

    fun showBiometricPrompt(activity: Activity, description: String, onSuccess: () -> Unit)

    companion object {

        const val ArgumentKeyPurpose = "purpose"
        const val ArgumentKeyPassCode = "passCode"
        const val ResultKeyPassCodeEntered = "passEntered"
    }
}