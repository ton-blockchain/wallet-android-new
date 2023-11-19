package org.ton.wallet.domain.settings.impl

import android.content.SharedPreferences
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.domain.settings.api.DeleteWalletUseCase

class DeleteWalletUseCaseImpl(
    private val defaultPreferences: SharedPreferences,
    private val securedPreferences: SharedPreferences,
    private val repositories: List<BaseRepository>
) : DeleteWalletUseCase {

    override suspend fun invoke() {
        repositories.forEach { it.deleteWallet() }
        defaultPreferences.edit().clear().apply()
        securedPreferences.edit().clear().apply()
    }
}