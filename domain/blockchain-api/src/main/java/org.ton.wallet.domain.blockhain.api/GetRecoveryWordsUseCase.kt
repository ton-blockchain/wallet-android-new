package org.ton.wallet.domain.blockhain.api

interface GetRecoveryWordsUseCase {

    val wordsCount: Int

    @Throws(Exception::class)
    suspend fun getHints(): Array<String>
}