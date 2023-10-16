package org.ton.wallet.app.screen

import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.feature.tonconnect.api.TonConnectApproveScreenApi

class TonConnectApproveScreenApiImpl(
    private val navigator: Navigator
) : TonConnectApproveScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }
}