package org.ton.wallet.domain.settings.impl

import android.content.SharedPreferences
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.domain.settings.api.DeleteWalletUseCase
import org.ton.wallet.lib.tonconnect.TonConnectClient

class DeleteWalletUseCaseImpl(
    private val defaultPreferences: SharedPreferences,
    private val securedPreferences: SharedPreferences,
    private val repositories: List<BaseRepository>,
    private val tonConnectClient: TonConnectClient
) : DeleteWalletUseCase {

    override suspend fun invoke() {
        tonConnectClient.disconnectAllClients()
        repositories.forEach { it.deleteWallet() }
        defaultPreferences.edit().clear().apply()
        securedPreferences.edit().clear().apply()
    }
}