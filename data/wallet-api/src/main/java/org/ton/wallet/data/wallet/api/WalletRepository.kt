package org.ton.wallet.data.wallet.api

import kotlinx.coroutines.flow.StateFlow
import org.ton.wallet.data.core.BaseRepository

interface WalletRepository : BaseRepository {

    val hasWalletFlow: StateFlow<Boolean>
    val publicKey: String
    val seed: ByteArray

    @Throws(Exception::class)
    suspend fun createWallet(words: Array<String>?)

    fun getRecoveryPhrase(): List<String>
}