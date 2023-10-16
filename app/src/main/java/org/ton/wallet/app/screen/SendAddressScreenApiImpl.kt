package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.domain.blockhain.api.AddressType
import org.ton.wallet.feature.scanqr.impl.ScanQrScreenArguments
import org.ton.wallet.feature.send.api.SendAddressScreenApi
import org.ton.wallet.feature.send.impl.amount.SendAmountScreenArguments

internal class SendAddressScreenApiImpl(
    private val navigator: Navigator
) : SendAddressScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToScanQr() {
        navigator.push(ScanQrScreenArguments(arrayOf(ScanQrScreenArguments.LinkActionTypeTransfer)))
    }

    override fun navigateToSendAmount(addressType: AddressType, amount: Long?, message: String?) {
        navigator.push(SendAmountScreenArguments(
            ufAddress = addressType.ufAddress ?: "",
            dns = when (addressType) {
                is AddressType.DnsAddress -> addressType.dns
                else -> null
            },
            amount = amount,
            message = message
        ))
    }
}