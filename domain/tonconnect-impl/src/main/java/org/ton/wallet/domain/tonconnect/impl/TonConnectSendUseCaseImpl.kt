package org.ton.wallet.domain.tonconnect.impl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.wallet.data.core.connect.TonConnect
import org.ton.wallet.data.tonconnect.api.TonConnectRepository
import org.ton.wallet.domain.tonconnect.api.TonConnectSendUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class TonConnectSendUseCaseImpl(
    private val json: Json,
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase,
    private val tonConnectRepository: TonConnectRepository
) : TonConnectSendUseCase {

    override suspend fun sendTransactionResult(clientId: String, success: TonConnect.SendTransactionResponse.Success) {
        val accountId = getCurrentAccountDataUseCase.getAccountState()?.id
            ?: throw IllegalStateException("Account is null")
        val responseJson = json.encodeToString(success)
        tonConnectRepository.sendMessage(accountId, clientId, responseJson.toByteArray())
    }

    override suspend fun sendTransactionError(clientId: String, error: TonConnect.SendTransactionResponse.Error) {
        val accountId = getCurrentAccountDataUseCase.getAccountState()?.id
            ?: throw IllegalStateException("Account is null")
        val responseJson = json.encodeToString(error)
        tonConnectRepository.sendMessage(accountId, clientId, responseJson.toByteArray())
    }
}