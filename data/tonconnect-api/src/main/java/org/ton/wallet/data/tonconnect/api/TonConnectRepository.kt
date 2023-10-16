package org.ton.wallet.data.tonconnect.api

import kotlinx.coroutines.flow.Flow
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.data.core.connect.TonConnect
import org.ton.wallet.data.tonconnect.api.model.TonConnectEvent

interface TonConnectRepository : BaseRepository {

    @Throws(Exception::class)
    suspend fun getManifestInfo(manifestUrl: String): TonConnect.Manifest?

    suspend fun checkExistingConnections(accountId: Int)

    @Throws(Exception::class)
    suspend fun connect(accountId: Int, clientId: String)

    suspend fun sendMessage(accountId: Int, clientId: String, body: ByteArray)

    fun getEventsFlow(accountId: Int): Flow<TonConnectEvent>
}