package org.ton.wallet.feature.send.api

interface SendConfirmScreenApi {

    fun navigateBack()

    fun navigateToSendProcessing(address: String, amount: Long, isAllAmount: Boolean, fee: Long, message: String?)

    fun navigateToPassCodeEnter(purpose: String)
}