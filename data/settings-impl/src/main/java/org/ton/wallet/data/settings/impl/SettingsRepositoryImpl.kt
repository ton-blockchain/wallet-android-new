package org.ton.wallet.data.settings.impl

import android.content.SharedPreferences
import kotlinx.coroutines.flow.*
import org.ton.wallet.data.core.DefaultPrefsKeys
import org.ton.wallet.data.core.model.FiatCurrency
import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.data.settings.api.SettingsRepository

class SettingsRepositoryImpl(
    private val preferences: SharedPreferences
) : SettingsRepository {

    private val isNotificationsTurnedOnFlow = MutableStateFlow(getIsNotificationsTurnedOn())

    override val accountTypeFlow = MutableStateFlow(getAccountType())
    override val fiatCurrencyFlow = MutableStateFlow(getFiatCurrency())
    override val hasNotificationsPermissionFlow = MutableStateFlow(false)

    override val isNotificationsOn: StateFlow<Boolean> = combine(
        hasNotificationsPermissionFlow,
        isNotificationsTurnedOnFlow
    ) { hasPermission, isTurnedOn ->
        hasPermission && isTurnedOn
    }.stateIn(CoroutineScopes.repositoriesScope, SharingStarted.Eagerly, false)

    override val isNotificationPermissionDialogShown: Boolean
        get() = preferences.getBoolean(DefaultPrefsKeys.NotificationsPermissionDialog, false)

    override val isRecoveryChecked: Boolean
        get() = preferences.getBoolean(DefaultPrefsKeys.RecoveryChecked, false)

    override fun setAccountType(accountType: TonAccountType) {
        preferences.edit().putString(DefaultPrefsKeys.AccountTypeSelected, accountType.name).apply()
        accountTypeFlow.tryEmit(accountType)
    }

    override fun setFiatCurrency(fiatCurrency: FiatCurrency) {
        preferences.edit().putString(DefaultPrefsKeys.FiatCurrency, fiatCurrency.name).apply()
        fiatCurrencyFlow.value = fiatCurrency
    }

    override fun setNotificationsTurnedOn(isNotificationsOn: Boolean) {
        preferences.edit().putBoolean(DefaultPrefsKeys.Notifications, isNotificationsOn).apply()
        isNotificationsTurnedOnFlow.value = isNotificationsOn
    }

    override fun setRecoveryChecked() {
        preferences.edit().putBoolean(DefaultPrefsKeys.RecoveryChecked, true).apply()
    }

    override fun setNotificationsDialogShown() {
        preferences.edit().putBoolean(DefaultPrefsKeys.NotificationsPermissionDialog, true).apply()
    }

    override fun setNotificationsPermission(isPermitted: Boolean) {
        hasNotificationsPermissionFlow.value = isPermitted
    }

    override suspend fun deleteWallet() {
        preferences.edit().putString(DefaultPrefsKeys.AccountTypeSelected, null).apply()
        accountTypeFlow.tryEmit(null)
    }

    private fun getAccountType(): TonAccountType? {
        val accountTypeString = preferences.getString(DefaultPrefsKeys.AccountTypeSelected, null) ?: return null
        return TonAccountType.valueOf(accountTypeString)
    }

    private fun getFiatCurrency(): FiatCurrency {
        val fiatCurrencyString = preferences.getString(DefaultPrefsKeys.FiatCurrency, DefaultFiatCurrency.name)
        return FiatCurrency.valueOf(fiatCurrencyString!!)
    }

    private fun getIsNotificationsTurnedOn(): Boolean {
        return preferences.getBoolean(DefaultPrefsKeys.Notifications, true)
    }

    private companion object {

        private val DefaultFiatCurrency = FiatCurrency.USD
    }
}