package org.ton.wallet.data.settings.api

import kotlinx.coroutines.flow.StateFlow
import org.ton.wallet.data.core.BaseRepository
import org.ton.wallet.data.core.model.FiatCurrency
import org.ton.wallet.data.core.model.TonAccountType

interface SettingsRepository : BaseRepository {

    val accountTypeFlow: StateFlow<TonAccountType?>
    val fiatCurrencyFlow: StateFlow<FiatCurrency>
    val hasNotificationsPermissionFlow: StateFlow<Boolean>
    val isNotificationsOn: StateFlow<Boolean>

    val isNotificationPermissionDialogShown: Boolean
    val isRecoveryChecked: Boolean

    fun setAccountType(accountType: TonAccountType)

    fun setFiatCurrency(fiatCurrency: FiatCurrency)

    fun setNotificationsTurnedOn(isNotificationsOn: Boolean)

    fun setNotificationsDialogShown()

    fun setNotificationsPermission(isPermitted: Boolean)

    fun setRecoveryChecked()

    companion object {

        val DefaultAccountType = TonAccountType.v4r2
    }
}