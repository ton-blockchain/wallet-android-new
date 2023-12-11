package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.scanqr.impl.ScanQrScreenArguments
import org.ton.wallet.feature.send.impl.address.SendAddressScreenArguments
import org.ton.wallet.feature.transactions.impl.TransactionDetailsScreenArguments
import org.ton.wallet.feature.wallet.api.MainScreenApi
import org.ton.wallet.screen.AppScreen

internal class MainScreenApiImpl(
    private val navigator: Navigator
) : MainScreenApi {

    override fun navigateToMain() {
        navigator.popTo(AppScreen.Main.name)
    }

    override fun navigateToScanQr() {
        val actionTypes = arrayOf(
            ScanQrScreenArguments.LinkActionTypeTransfer,
            ScanQrScreenArguments.LinkActionTypeTonConnect
        )
        navigator.push(ScanQrScreenArguments(actionTypes))
    }

    override fun navigateToSettings() {
        navigator.push(AppScreen.Settings.name)
    }

    override fun navigateToReceive() {
        navigator.push(AppScreen.Receive.name)
    }

    override fun navigateToSend() {
        navigator.push(SendAddressScreenArguments())
    }

    override fun navigateToTransactionsDetails(internalId: Long, isMultiMessage: Boolean) {
        navigator.push(TransactionDetailsScreenArguments(internalId, isMultiMessage))
    }
}