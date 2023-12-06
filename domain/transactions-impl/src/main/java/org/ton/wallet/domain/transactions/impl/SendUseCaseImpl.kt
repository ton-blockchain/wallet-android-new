package org.ton.wallet.domain.transactions.impl

import org.ton.wallet.core.ext.clear
import org.ton.wallet.data.core.model.MessageData
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.SendParams
import org.ton.wallet.data.transactions.api.model.SendResult
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.transactions.api.SendUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class SendUseCaseImpl(
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase,
    private val transactionsRepository: TransactionsRepository,
    private val walletRepository: WalletRepository
) : SendUseCase {

    override suspend fun invoke(messages: List<MessageData>): SendResult {
        val account = getCurrentAccountDataUseCase.getAccountState() ?: throw Exception("Account is null")
        val seed = walletRepository.seed
        val sendParams = SendParams(
            account = account,
            publicKey = walletRepository.publicKey,
            messages = messages,
            seed = seed,
        )
        val result = transactionsRepository.performSend(sendParams)
        seed.clear()
        return result
    }
}