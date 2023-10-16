package org.ton.wallet.domain.transactions.impl

import org.ton.wallet.core.ext.clear
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

    override suspend fun invoke(toAddress: String, amount: Long, message: String?): Long {
        val account = getCurrentAccountDataUseCase.getAccountState() ?: throw Exception("Account is null")
        val secret = walletRepository.secret
        val password = walletRepository.password
        val seed = walletRepository.seed
        val sendParams = SendParams(account.address, toAddress, amount, message)
        val result = transactionsRepository.performSend(account, walletRepository.publicKey, secret, password, seed, sendParams)
        secret.clear()
        password.clear()
        seed.clear()
        return result
    }
}