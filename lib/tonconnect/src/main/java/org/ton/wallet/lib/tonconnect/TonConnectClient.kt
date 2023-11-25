package org.ton.wallet.lib.tonconnect

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

interface TonConnectClient {

    val eventsFlow: Flow<TonConnectEvent>

    @Throws(Exception::class)
    suspend fun restoreConnections()

    @Throws(Exception::class)
    suspend fun connect(clientId: String)

    @Throws(Exception::class)
    suspend fun disconnect(clientId: String, withClearData: Boolean = true)

    @Throws(Exception::class)
    suspend fun sendMessage(clientId: String, jsonString: String)

    suspend fun disconnectAllClients()

    @Throws(Exception::class)
    suspend fun getManifest(url: String): TonConnectApi.AppManifest

    fun setEventShowed(clientId: String, eventId: Long)


    companion object {

        fun createInstance(json: Json, sharedPreferences: SharedPreferences, securedPreferences: SharedPreferences): TonConnectClient {
            return TonConnectClientImpl(json, sharedPreferences, securedPreferences)
        }
    }
}