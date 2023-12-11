package org.ton.wallet.feature.wallet.api

interface MainScreenApi {

    fun navigateToMain()

    fun navigateToScanQr()

    fun navigateToSettings()

    fun navigateToReceive()

    fun navigateToSend()

    fun navigateToTransactionsDetails(internalId: Long, isMultiMessage: Boolean)
}