package org.ton.wallet.feature.onboarding.api

interface RecoveryShowScreenApi {

    fun navigateToCheck()

    fun navigateBack()

    fun popToScreenKeepCurrent(screenName: String)
}