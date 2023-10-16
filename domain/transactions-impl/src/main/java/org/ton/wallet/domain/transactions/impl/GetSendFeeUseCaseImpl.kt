package org.ton.wallet.domain.transactions.impl

import org.ton.wallet.data.tonclient.api.TonApiException
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.api.model.SendParams
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.transactions.api.GetSendFeeUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase

class GetSendFeeUseCaseImpl(
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase,
    private val transactionsRepository: TransactionsRepository,
    private val walletRepository: WalletRepository
) : GetSendFeeUseCase {

    override suspend fun invoke(toAddress: String, amount: Long, message: String?): Long {
        val account = getCurrentAccountDataUseCase.getAccountState() ?: throw IllegalArgumentException("Account is null")
        val sendParams = SendParams(account.address, toAddress, amount, message)
        return try {
            transactionsRepository.getSendFee(account, walletRepository.publicKey, sendParams)
        } catch (e: TonApiException) {
            if (e.message == "NOT_ENOUGH_FUNDS") {
                0
            } else {
                throw e
            }
        }
    }
}