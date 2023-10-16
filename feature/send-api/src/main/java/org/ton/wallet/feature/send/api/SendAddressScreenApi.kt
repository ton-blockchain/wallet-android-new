package org.ton.wallet.feature.send.api

import org.ton.wallet.domain.blockhain.api.AddressType

interface SendAddressScreenApi {

    fun navigateBack()

    fun navigateToScanQr()

    fun navigateToSendAmount(addressType: AddressType, amount: Long?, message: String?)
}