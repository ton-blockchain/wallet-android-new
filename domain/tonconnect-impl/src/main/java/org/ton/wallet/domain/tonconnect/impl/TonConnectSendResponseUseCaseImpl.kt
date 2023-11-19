package org.ton.wallet.domain.tonconnect.impl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.wallet.domain.tonconnect.api.TonConnectSendResponseUseCase
import org.ton.wallet.lib.tonconnect.TonConnectApi
import org.ton.wallet.lib.tonconnect.TonConnectClient

class TonConnectSendResponseUseCaseImpl(
    private val json: Json,
    private val tonConnectClient: TonConnectClient
) : TonConnectSendResponseUseCase {

    override suspend fun sendResponse(clientId: String, response: TonConnectApi.SendTransactionResponse) {
        tonConnectClient.sendMessage(clientId, json.encodeToString(response))
    }
}