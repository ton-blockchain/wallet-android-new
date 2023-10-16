package org.ton.wallet.domain.blockhain.impl

import drinkless.org.ton.TonApi
import org.ton.wallet.data.tonclient.api.TonClient
import org.ton.wallet.data.tonclient.api.sendRequestTyped
import org.ton.wallet.domain.blockhain.api.GetRecoveryWordsUseCase

class GetRecoveryHintsUseCaseImpl(
    private val tonClient: TonClient
) : GetRecoveryWordsUseCase {

    override val wordsCount = 24

    @Throws(Exception::class)
    override suspend fun getHints(): Array<String> {
        return tonClient.sendRequestTyped<TonApi.Bip39Hints>(TonApi.GetBip39Hints()).words
    }
}