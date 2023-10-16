package org.ton.wallet.domain.wallet.api

interface CreateWalletUseCase {

    suspend fun invoke(words: Array<String>?)
}