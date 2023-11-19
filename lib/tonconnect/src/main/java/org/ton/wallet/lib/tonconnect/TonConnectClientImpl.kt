package org.ton.wallet.lib.tonconnect

import android.content.SharedPreferences
import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.*
import org.ton.wallet.core.ext.toHexByteArray
import org.ton.wallet.core.ext.toHexString
import org.ton.wallet.data.core.await
import org.ton.wallet.lib.log.L
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

internal class TonConnectClientImpl(
    private val json: Json,
    sharedPreferences: SharedPreferences,
    securedPreferences: SharedPreferences
) : EventSourceListener(), TonConnectClient {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable -> L.e(throwable) }
    private val coroutineScope = CoroutineScope(Dispatchers.IO + exceptionHandler)

    private val connectionClientIdMutexMap = ConcurrentHashMap<String, Mutex>()
    private val connectionHoldersMap = ConcurrentHashMap<String, ConnectionHolder>()

    private val store = TonConnectStore(sharedPreferences, securedPreferences)

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val _eventsFlow = MutableSharedFlow<TonConnectEvent>(replay = 1)
    override val eventsFlow: Flow<TonConnectEvent> = _eventsFlow

    // TonConnectClient
    override suspend fun restoreConnections() {
        val clientIds = store.getSavedClientIds()
        clientIds.forEach { clientId ->
            try {
                connect(clientId)
            } catch (e: Exception) {
                L.e(e)
            }
        }
    }

    override suspend fun connect(clientId: String) {
        val mutex = connectionClientIdMutexMap.getOrPut(clientId) { Mutex() }
        mutex.withLock {
            if (!connectionHoldersMap.containsKey(clientId)) {
                val publicKey: ByteArray
                val secretKey: ByteArray
                val storeKeys = store.getKeys(clientId)
                if (storeKeys == null) {
                    val cryptoBoxKeys = TonConnectCrypto.nativeCryptoBoxInitKeys()
                        ?: throw IllegalStateException("Could not create CryptoBox init keys")
                    publicKey = cryptoBoxKeys.copyOfRange(0, 32)
                    secretKey = cryptoBoxKeys.copyOfRange(32, 64)
                } else {
                    publicKey = storeKeys.first
                    secretKey = storeKeys.second
                }

                val httpUrl = BridgeHttpUrl.newBuilder()
                    .addPathSegment("events")
                    .addQueryParameter("client_id", publicKey.toHexString())
                    .build()
                val request = Request.Builder()
                    .addHeader("Accept", "text/event-stream")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Connection", "keep-alive")
                    .url(httpUrl)
                    .build()
                return suspendCoroutine { continuation ->
                    val eventSource = EventSources.createFactory(okHttpClient)
                        .newEventSource(request, this)
                    connectionHoldersMap[clientId] = ConnectionHolder(
                        clientId = clientId,
                        publicKey = publicKey,
                        secretKey = secretKey,
                        eventSource = eventSource,
                        connectionContinuation = continuation
                    )
                    store.saveConnection(clientId, publicKey, secretKey)
                }
            }
        }
    }

    override suspend fun sendMessage(clientId: String, jsonString: String) {
        val holder = connectionHoldersMap[clientId] ?: return
        val encryptedBody = TonConnectCrypto.nativeCryptoBox(jsonString.toByteArray(), clientId.toHexByteArray(), holder.secretKey)
        val encryptedBodyBase64 = Base64.encodeToString(encryptedBody, Base64.NO_WRAP)

        val httpUrl = BridgeHttpUrl.newBuilder()
            .addPathSegment("message")
            .addQueryParameter("client_id", holder.publicKey.toHexString())
            .addQueryParameter("to", clientId)
            .addQueryParameter("ttl", "300")
            .build()
        val request = Request.Builder()
            .post(encryptedBodyBase64.toRequestBody())
            .url(httpUrl)
            .build()

        L.d("TonConnectClient: sendMessage(clientId=$clientId, body=$jsonString)")
        okHttpClient.newCall(request).await()
    }

    override suspend fun disconnect(clientId: String) {
        val mutex = connectionClientIdMutexMap.getOrPut(clientId) { Mutex() }
        mutex.withLock {
            store.removeConnection(clientId)
            val holder = connectionHoldersMap.remove(clientId) ?: return
            try {
                holder.eventSource.cancel()
            } catch (e: Exception) {
                L.e(e)
            }
        }
    }

    override suspend fun getManifest(url: String): TonConnectApi.AppManifest {
        val httpUrl = url.toHttpUrlOrNull()!!
        val request = Request.Builder().url(httpUrl).build()
        val response = okHttpClient.newCall(request).await()
        val jsonResponse = response.body?.string() ?: ""
        return json.decodeFromString<TonConnectApi.AppManifest>(jsonResponse)
    }

    override fun setEventShowed(clientId: String, eventId: Long) {
        val currentEventId = store.getLastRequestId(clientId)
        if (eventId > currentEventId) {
            store.saveLastRequestId(clientId, eventId)
        }
    }


    // EventSourceListener
    override fun onOpen(eventSource: EventSource, response: Response) {
        super.onOpen(eventSource, response)
        val clientId = getClientId(eventSource) ?: return
        L.d("TonConnectClient onOpen clientId=$clientId")
        coroutineScope.launch {
            connectionHoldersMap[clientId]?.let { holder ->
                holder.connectionContinuation?.resume(Unit)
                holder.connectionContinuation = null
            }
        }
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        super.onEvent(eventSource, id, type, data)
        val clientId = getClientId(eventSource) ?: return
        val holder = connectionHoldersMap[clientId] ?: return
        L.d("TonConnectClient onEvent clientId=$clientId, id: $id, type: $type, data: $data")

        coroutineScope.launch {
            val bridgeData = json.decodeFromString<TonConnectApi.BridgeMessage>(data)
            val encodedMessageBytes = Base64.decode(bridgeData.message, Base64.NO_WRAP)
            val decodedMessageBytes = TonConnectCrypto.nativeCryptoBoxOpen(encodedMessageBytes, clientId.toHexByteArray(), holder.secretKey)
                ?: return@launch
            val decodedMessage = String(decodedMessageBytes)
            L.d("TonConnectClient onEvent decoded: $decodedMessage")
            val appRequest = json.decodeFromString<TonConnectApi.AppRequest>(decodedMessage)
            val lastRequestId = store.getLastRequestId(clientId)
            if (appRequest.id <= lastRequestId) {
                return@launch
            }

            val appRequestEvent: TonConnectApi.AppRequestEvent = when (appRequest.method) {
                TonConnectApi.AppRequest.MethodSendTransaction -> {
                    val jsonString = appRequest.params.getOrNull(0) ?: ""
                    json.decodeFromString<TonConnectApi.SendTransactionRequest>(jsonString)
                }
                TonConnectApi.AppRequest.MethodDisconnect -> {
                    TonConnectApi.DisconnectRequest()
                }
                else -> {
                    throw IllegalArgumentException("Unsupported appRequest.method=${appRequest.method}")
                }
            }

            if (appRequestEvent is TonConnectApi.SendTransactionRequest
                && appRequestEvent.network != null
                && appRequestEvent.network != TonConnectApi.NetworkMainnet.toString()
            ) {
                return@launch
            }

            val tonConnectEvent = TonConnectEvent(clientId, appRequest.id, appRequestEvent)
            _eventsFlow.emit(tonConnectEvent)
        }
    }

    override fun onClosed(eventSource: EventSource) {
        super.onClosed(eventSource)
        coroutineScope.launch {
            val clientId = getClientId(eventSource) ?: return@launch
            L.d("TonConnectClient onClosed clientId=$clientId")
            disconnect(clientId)
        }
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        super.onFailure(eventSource, t, response)
        coroutineScope.launch {
            val clientId = getClientId(eventSource) ?: return@launch
            L.d("TonConnectClient onFailure clientId=$clientId")
            disconnect(clientId)
        }
    }


    // private
    private fun getClientId(eventSource: EventSource): String? {
        return connectionHoldersMap.entries.firstOrNull { it.value.eventSource == eventSource }?.value?.clientId
    }

    private companion object {

        private val BridgeHttpUrl = "https://bridge.tonapi.io/bridge".toHttpUrl()
    }

    private class ConnectionHolder(
        val clientId: String,
        val publicKey: ByteArray,
        val secretKey: ByteArray,
        val eventSource: EventSource,
        var connectionContinuation: Continuation<Unit>?
    )
}