package org.ton.wallet.data.tonclient.impl

import android.content.SharedPreferences
import drinkless.org.ton.Client
import drinkless.org.ton.TonApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import org.ton.wallet.data.core.DefaultPrefsKeys
import org.ton.wallet.data.core.await
import org.ton.wallet.data.tonclient.api.TonApiException
import org.ton.wallet.data.tonclient.api.TonClient
import org.ton.wallet.lib.log.L
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.*
import kotlin.math.roundToInt

class TonClientImpl(
    private val sharedPreferences: SharedPreferences,
    private val okHttpClient: OkHttpClient,
    private val keysDirectory: File
) : TonClient {

    private val tonClientInitMutex = Mutex()
    private val isTonApiInitialized = AtomicBoolean(false)
    private val syncProgressFlow: StateFlow<Int> = MutableStateFlow(100)
    private val ton = Client.create(null, null, null)

    init {
        ton.setUpdatesHandler(::handleUpdate)
    }

    override suspend fun sendRequest(request: TonApi.Function): TonApi.Object {
        initClient()
        return sendRequestInternal(request)
    }

    @Throws(Exception::class)
    private suspend fun initClient() {
        if (isTonApiInitialized.get()) {
            return
        }
        tonClientInitMutex.withLock {
            if (isTonApiInitialized.get()) {
                return@withLock
            }

            val configJson = getConfigJson() ?: throw IllegalStateException("Config json is null")
            val tonApiConfig = TonApi.Config(configJson, BlockChainName, false, false)
            keysDirectory.mkdirs()
            val keyStoreType = TonApi.KeyStoreTypeDirectory(keysDirectory.absolutePath)
            val tonApiOptions = TonApi.Options(tonApiConfig, keyStoreType)
            val result = sendRequestInternal(TonApi.Init(tonApiOptions))
            if (result is TonApi.OptionsInfo) {
                isTonApiInitialized.set(true)
            }
        }
    }

    private suspend fun getConfigJson(): String? {
        return try {
            val request = Request.Builder().url(ConfigUrl).build()
            val response = okHttpClient.newCall(request).await()
            val jsonString = response.body?.string()
            if (jsonString != null) {
                sharedPreferences.edit().putString(DefaultPrefsKeys.TonConfigJson, jsonString).apply()
            }
            jsonString
        } catch (e: Exception) {
            sharedPreferences.getString(DefaultPrefsKeys.TonConfigJson, null)
        }
    }

    @Throws(Exception::class)
    private suspend fun sendRequestInternal(request: TonApi.Function): TonApi.Object {
        return suspendCoroutine { cont ->
            val exceptionHandler = Client.ExceptionHandler(cont::resumeWithException)
            val resultHandler = object : Client.ResultHandler {
                override fun onResult(result: TonApi.Object) {
                    if (result is TonApi.Error) {
                        L.e("TonApi.Error ${result.code}: ${result.message}")
//                        val retryRequest = result.message.startsWith("LITE_SERVER_NOTREADY")
//                                || result.message.contains("LITE_SERVER_UNKNOWN: block is not applied")
                        val retryRequest = false
                        if (retryRequest) {
                            L.d("Retry send request")
                            trySendRequest(cont, request, this, exceptionHandler)
                        } else {
                            cont.resumeWithException(TonApiException(result))
                        }
                    } else {
                        cont.resume(result)
                    }
                }
            }
            trySendRequest(cont, request, resultHandler, exceptionHandler)
        }
    }

    private fun trySendRequest(
        continuation: Continuation<TonApi.Object>,
        request: TonApi.Function,
        resultHandler: Client.ResultHandler,
        exceptionHandler: Client.ExceptionHandler
    ) {
        try {
            ton.send(request, resultHandler, exceptionHandler)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    private fun handleUpdate(obj: TonApi.Object) {
        if (obj is TonApi.UpdateSyncState) {
            if (obj.syncState is TonApi.SyncStateInProgress) {
                val syncState = obj.syncState as TonApi.SyncStateInProgress
                val progress = (syncState.currentSeqno - syncState.fromSeqno).toFloat() / (syncState.toSeqno - syncState.fromSeqno) * 100f
                (syncProgressFlow as MutableStateFlow).tryEmit(progress.roundToInt())
            } else if (obj.syncState is TonApi.SyncStateDone) {
                (syncProgressFlow as MutableStateFlow).tryEmit(100)
            }
        }
    }

    private companion object {
        private const val BlockChainName = "mainnet"
        private const val ConfigUrl = "https://ton.org/global-config-wallet.json"
    }
}