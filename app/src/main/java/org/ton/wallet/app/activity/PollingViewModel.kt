package org.ton.wallet.app.activity

import android.content.SharedPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ton.wallet.app.Injector
import org.ton.wallet.app.action.TonConnectEventHandler
import org.ton.wallet.data.core.DefaultPrefsKeys
import org.ton.wallet.data.prices.api.PricesRepository
import org.ton.wallet.data.tonconnect.api.TonConnectRepository
import org.ton.wallet.domain.tonconnect.api.TonConnectGetEventsUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.domain.wallet.api.RefreshCurrentAccountStateUseCase
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.viewmodel.BaseViewModel

class PollingViewModel : BaseViewModel() {

    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase by inject()
    private val refreshCurrentAccountStateUseCase: RefreshCurrentAccountStateUseCase by inject()
    private val tonConnectRepository: TonConnectRepository by inject()
    private val tonConnectEventHandler: TonConnectEventHandler by inject()
    private val tonConnectGetEventsUseCase: TonConnectGetEventsUseCase by inject()

    private val jobsMap = mutableMapOf<String, Job>()
    private val pricesRepository: PricesRepository by inject()
    private val preferences: SharedPreferences by inject(Injector.DefaultSharedPreferences)

    fun start() {
        initPolling("fiatPrices", 60_000) {
            pricesRepository.fetchPrices()
        }
        initPolling("accountState", 60_000) {
            try {
                refreshCurrentAccountStateUseCase.invoke()
            } catch (e: Exception) {
                L.e(e)
            }
        }

        getCurrentAccountDataUseCase.getIdFlow()
            .onEach(tonConnectRepository::checkExistingConnections)
            .launchIn(viewModelScope + Dispatchers.IO)

        tonConnectGetEventsUseCase.invoke()
            .onEach(tonConnectEventHandler::onTonConnectEvent)
            .launchIn(viewModelScope + Dispatchers.IO)
    }

    private fun initPolling(name: String, intervalMillis: Long, action: suspend () -> Unit) {
        jobsMap[name]?.cancel()
        val job = viewModelScope.launch(Dispatchers.IO) {
            val lastUpdateTimeMillis = preferences.getLong(getPreferencesKey(name), 0)
            val timeSinceLastUpdateMillis = System.currentTimeMillis() - lastUpdateTimeMillis
            var delayMillis =
                if (timeSinceLastUpdateMillis > intervalMillis) 0
                else intervalMillis - timeSinceLastUpdateMillis
            var failureDelay = DefaultFailureDelayMillis
            while (isActive) {
                runCatching {
                    delay(delayMillis)
                    L.d("polling $name")
                    action.invoke()
                }.onFailure { throwable ->
                    L.e(throwable)
                    delay(failureDelay)
                    failureDelay *= 2
                }.onSuccess {
                    failureDelay = DefaultFailureDelayMillis
                    delayMillis = intervalMillis
                    preferences.edit().putLong(getPreferencesKey(name), System.currentTimeMillis()).apply()
                }
            }
        }
        jobsMap[name] = job
    }

    private fun getPreferencesKey(key: String): String {
        return DefaultPrefsKeys.PollingPrefix + key
    }

    private companion object {
        private const val DefaultFailureDelayMillis = 1_000L
    }
}