package org.ton.wallet.domain.transactions.api

interface GetSendFeeUseCase {

    suspend fun invoke(toAddress: String, amount: Long, message: String?): Long
}