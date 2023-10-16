package org.ton.wallet.domain.transactions.api

interface SendUseCase {

    suspend fun invoke(toAddress: String, amount: Long, message: String?): Long
}