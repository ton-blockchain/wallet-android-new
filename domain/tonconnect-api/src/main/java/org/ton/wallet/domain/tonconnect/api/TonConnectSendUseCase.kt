package org.ton.wallet.domain.tonconnect.api

import org.ton.wallet.data.core.connect.TonConnect

interface TonConnectSendUseCase {

    suspend fun sendTransactionResult(clientId: String, success: TonConnect.SendTransactionResponse.Success)

    suspend fun sendTransactionError(clientId: String, error: TonConnect.SendTransactionResponse.Error)
}