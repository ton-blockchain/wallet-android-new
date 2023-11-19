package org.ton.wallet.domain.transactions.impl

import org.ton.wallet.core.ext.clear
import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.SendParams
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.transactions.api.SendUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class SendUseCaseImpl(
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase,
    private val transactionsRepository: TransactionsRepository,
    private val walletRepository: WalletRepository
) : SendUseCase {

    override suspend fun invoke(toAddress: String, amount: Long, message: MessageData?, stateInitBase64: String?): Long {
        val account = getCurrentAccountDataUseCase.getAccountState() ?: throw Exception("Account is null")
        val secret = walletRepository.secret
        val password = walletRepository.password
        val seed = walletRepository.seed
        val sendParams = SendParams(
            account = account,
            publicKey = walletRepository.publicKey,
            toAddress = toAddress,
            amount = amount,
            message = message,
            stateInitBase64 = stateInitBase64,
            secret = secret,
            password = password,
            seed = seed,
        )
        val result = transactionsRepository.performSend(sendParams)
        secret.clear()
        password.clear()
        seed.clear()
        return result
    }
}