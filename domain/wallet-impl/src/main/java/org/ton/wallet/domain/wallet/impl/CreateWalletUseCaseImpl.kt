package org.ton.wallet.domain.wallet.impl

import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.wallet.api.CreateWalletUseCase

class CreateWalletUseCaseImpl(
    private val accountsRepository: AccountsRepository,
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository
) : CreateWalletUseCase {

    override suspend fun invoke(words: Array<String>?) {
        walletRepository.createWallet(words)
        TonAccountType.entries.forEach { type ->
            accountsRepository.createAccount(type)
        }
        settingsRepository.setAccountType(SettingsRepository.DefaultAccountType)
        if (!words.isNullOrEmpty()) {
            settingsRepository.setRecoveryChecked()
        }
    }
}