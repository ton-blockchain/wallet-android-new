package org.ton.wallet.domain.tonconnect.api

import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.lib.tonconnect.TonConnectApi

interface TonConnectOpenConnectionUseCase {

    suspend fun invoke(action: LinkAction.TonConnectAction, manifest: TonConnectApi.AppManifest)
}