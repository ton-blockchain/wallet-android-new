package org.ton.wallet.feature.send.api

interface SendConnectConfirmScreenApi {

    fun navigateBack()

    fun navigateToPassCodeEnter(purpose: String)
}