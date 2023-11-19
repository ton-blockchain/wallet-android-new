package org.ton.wallet.domain.tonconnect.api

import org.ton.wallet.lib.tonconnect.TonConnectApi

interface TonConnectSendResponseUseCase {

    suspend fun sendResponse(clientId: String, response: TonConnectApi.SendTransactionResponse)
}