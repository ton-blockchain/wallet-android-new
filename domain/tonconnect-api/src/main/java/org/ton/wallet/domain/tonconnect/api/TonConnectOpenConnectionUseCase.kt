package org.ton.wallet.domain.tonconnect.api

import org.ton.wallet.data.core.connect.TonConnect
import org.ton.wallet.data.core.link.LinkAction

interface TonConnectOpenConnectionUseCase {

    suspend fun invoke(action: LinkAction.TonConnectAction, manifest: TonConnect.Manifest)
}