package org.ton.wallet.app.activity

import android.content.SharedPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ton.wallet.app.Injector
import org.ton.wallet.app.action.TonConnectEventHandler
import org.ton.wallet.data.core.DefaultPrefsKeys
import org.ton.wallet.data.prices.api.PricesRepository
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.domain.wallet.api.RefreshCurrentAccountStateUseCase
import org.ton.wallet.lib.log.L
import org.ton.wallet.lib.tonconnect.TonConnectClient
import org.ton.wallet.screen.viewmodel.BaseViewModel

class PollingViewModel : BaseViewModel() {

    private val refreshCurrentAccountStateUseCase: RefreshCurrentAccountStateUseCase by inject()
    private val tonConnectEventHandler: TonConnectEventHandler by inject()
    private val tonConnectClient: TonConnectClient by inject()
    private val pricesRepository: PricesRepository by inject()
    private val preferences: SharedPreferences by inject(Injector.DefaultSharedPreferences)
    private val settingsRepository: SettingsRepository by inject()

    private val jobsMap = mutableMapOf<String, Job>()

    fun start() {
        initPolling("fiatPrices", 60_000) {
            pricesRepository.fetchPrices()
        }
        initPolling("accountState", 30_000) {
            try {
                refreshCurrentAccountStateUseCase.invoke()
            } catch (e: Exception) {
                L.e(e)
            }
        }

        settingsRepository.accountTypeFlow
            .onEach {
                try {
                    refreshCurrentAccountStateUseCase.invoke()
                } catch (e: Exception) {
                    L.e(e)
                }
            }
            .launchIn(viewModelScope + Dispatchers.IO)

        tonConnectClient.eventsFlow
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