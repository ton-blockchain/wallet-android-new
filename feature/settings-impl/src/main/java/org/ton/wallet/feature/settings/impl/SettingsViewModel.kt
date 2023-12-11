package org.ton.wallet.feature.settings.impl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.Res
import org.ton.wallet.core.ext.weak
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.data.core.BuildConfig
import org.ton.wallet.data.core.model.FiatCurrency
import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.data.notifications.api.NotificationsRepository
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.wallet.api.AccountsRepository
import org.ton.wallet.domain.settings.api.DeleteWalletUseCase
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.passcode.api.PassCodeSetupScreenApi
import org.ton.wallet.feature.settings.api.SettingsScreenApi
import org.ton.wallet.feature.settings.impl.adapter.SettingsSwitchItem
import org.ton.wallet.lib.security.BiometricUtils
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.popup.MenuPopupWindow
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uicomponents.vh.SettingsHeaderItem
import org.ton.wallet.uicomponents.vh.SettingsTextUiItem
import org.ton.wallet.uikit.RUiKitColor
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class SettingsViewModel : BaseViewModel() {

    private val accountsRepository: AccountsRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val context: Context by inject()
    private val deleteWalletUseCase: DeleteWalletUseCase by inject()
    private val notificationsRepository: NotificationsRepository by inject()
    private val screenApi: SettingsScreenApi by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val snackBarController: SnackBarController by inject()

    private val accountTypeAddressMap = ConcurrentHashMap<TonAccountType, String?>()

    private var activityRef: WeakReference<FragmentActivity?> = weak(null)

    private val _showDeleteWalletDialogFlow = Channel<Unit>(Channel.BUFFERED)
    val showDeleteWalletDialogFlow: Flow<Unit> = _showDeleteWalletDialogFlow.receiveAsFlow()

    private val _showAddressPopupItemsFlow = Channel<List<MenuPopupWindow.MenuPopupItem>>(Channel.BUFFERED)
    val showAddressPopupItemsFlow: Flow<List<MenuPopupWindow.MenuPopupItem>> = _showAddressPopupItemsFlow.receiveAsFlow()

    private val _showFiatCurrencyPopupItemsFlow = Channel<List<MenuPopupWindow.MenuPopupItem>>(Channel.BUFFERED)
    val showFiatCurrencyPopupItemsFlow: Flow<List<MenuPopupWindow.MenuPopupItem>> = _showFiatCurrencyPopupItemsFlow.receiveAsFlow()

    private val isBiometricOnFlow: StateFlow<Boolean> = authRepository.isBiometricActiveFlow

    val itemsFlow: Flow<List<Any>> = combine(
        settingsRepository.accountTypeFlow,
        settingsRepository.fiatCurrencyFlow,
        isBiometricOnFlow,
        settingsRepository.isNotificationsOn
    ) { accountType, fiatCurrency, isBiometricOn, isNotificationsOn ->
        val items = mutableListOf(
            SettingsHeaderItem(Res.str(RString.general)),
            SettingsSwitchItem(ItemNotifications, Res.str(RString.notifications), isNotificationsOn),
            SettingsTextUiItem(ItemAddress, Res.str(RString.active_address), accountType?.getString()),
            SettingsTextUiItem(ItemCurrency, Res.str(RString.primary_currency), fiatCurrency.name.uppercase()),
            SettingsHeaderItem(Res.str(RString.security)),
            SettingsTextUiItem(ItemRecoveryPhrase, Res.str(RString.show_recovery_phrase)),
            SettingsTextUiItem(ItemChangePasscode, Res.str(RString.change_passcode)),
        )
        if (BiometricUtils.isBiometricsAvailableOnDevice(Res.context)) {
            items.add(SettingsSwitchItem(ItemBiometricAuth, Res.str(RString.biometric_auth), isBiometricOn))
        }
        items.add(SettingsTextUiItem(ItemDeleteWallet, Res.str(RString.delete_wallet), titleColor = Res.color(RUiKitColor.text_error)))

        if (BuildConfig.DEBUG) {
            items.add(SettingsHeaderItem("Debug"))
            items.add(SettingsTextUiItem(ItemExportLogs, "Export logs"))
        }

        items
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val deferred = TonAccountType.entries.map { type ->
                async { accountsRepository.getAccountAddress(type) }
            }
            deferred.awaitAll().forEachIndexed { index, s ->
                val type = TonAccountType.entries[index]
                accountTypeAddressMap[type] = s
            }
        }
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == PassCodeEnterScreenApi.ResultKeyPassCodeEntered) {
            when (args?.getString(PassCodeEnterScreenApi.ArgumentKeyPurpose)) {
                PurposeRecovery -> {
                    screenApi.navigateToRecovery()
                }
                PurposeChangePasscode -> {
                    screenApi.navigateChangePassCode()
                }
                PurposeChangeBiometric -> {
                    screenApi.navigateBack()
                    if (isBiometricOnFlow.value) {
                        authRepository.setBiometricAuthOn(false)
                    } else {
                        activityRef.get()?.let { activity ->
                            screenApi.showBiometricPrompt(activity) {
                                authRepository.setBiometricAuthOn(true)
                            }
                        }
                    }
                }
            }
        } else if (code == PassCodeSetupScreenApi.ResultCodePassCodeSet) {
            screenApi.navigateToSettings()
            val message = SnackBarMessage(
                title = null,
                message = Res.str(RString.passcode_changed)
            )
            snackBarController.showMessage(message)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, permissions: MutableList<String>) {
        super.onPermissionsGranted(requestCode, permissions)
        if (requestCode == PermissionRequestIdNotifications
            && !settingsRepository.hasNotificationsPermissionFlow.value
            && permissions.contains(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            settingsRepository.setNotificationsTurnedOn(true)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, permissions: MutableList<String>) {
        super.onPermissionsDenied(requestCode, permissions)
        if (requestCode == PermissionRequestIdNotifications) {
            screenApi.showAppNotificationsSettings(context, notificationsRepository.defaultChannelId)
        }
    }

    fun setActivity(activity: FragmentActivity) {
        activityRef = weak(activity)
    }

    fun onTextItemClicked(activity: Activity, item: SettingsTextUiItem) {
        when (item.id) {
            ItemAddress -> onAddressClicked(item)
            ItemCurrency -> onFiatCurrencyClicked()
            ItemRecoveryPhrase -> onShowRecoveryClicked()
            ItemChangePasscode -> onChangePasscodeClicked()
            ItemDeleteWallet -> _showDeleteWalletDialogFlow.trySend(Unit)
            ItemExportLogs -> screenApi.shareLogs(activity)
        }
    }

    fun onSwitchItemClicked(activity: Activity, item: SettingsSwitchItem) {
        when (item.id) {
            ItemNotifications -> onNotificationsClicked(activity, item.isChecked)
            ItemBiometricAuth -> onBiometricAuthClicked(item.isChecked)
        }
    }

    fun onDeleteWalletClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteWalletUseCase.invoke()
            screenApi.navigateToStart()
        }
    }

    private fun onNotificationsClicked(activity: Activity, isChecked: Boolean) {
        if (!isChecked && !settingsRepository.hasNotificationsPermissionFlow.value) {
            val request = notificationsRepository.getPermissionRequest(activity, PermissionRequestIdNotifications)
            request?.let {
                EasyPermissions.requestPermissions(it)
                screenApi.onPermissionRequested()
            }
            return
        }
        settingsRepository.setNotificationsTurnedOn(!isChecked)
    }

    private fun onBiometricAuthClicked(isChecked: Boolean) {
        val context = activityRef.get() ?: return
        if (!isChecked && BiometricUtils.isBiometricsNoneEnrolled(context)) {
            screenApi.showBiometricEnrollAlert(context)
            return
        }
        screenApi.navigateToPassCodeEnter(PurposeChangeBiometric, withBiometrics = false)
    }

    private fun getAccountAddress(type: TonAccountType): String? {
        return accountTypeAddressMap[type]
    }

    private fun onAddressClicked(item: SettingsTextUiItem) {
        val items = TonAccountType.entries.map { type ->
            val typeString = type.getString()
            val addressString = Formatter.getShortAddressSafe(getAccountAddress(type))
            val stringBuilder = SpannableStringBuilder(typeString).append("  ")
            if (addressString != null) {
                stringBuilder.append(addressString)
            }
            stringBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.text_primary)), 0, typeString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            stringBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.text_secondary)), typeString.length + 1, stringBuilder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            MenuPopupWindow.MenuPopupItem(stringBuilder) { settingsRepository.setAccountType(type) }
        }
        _showAddressPopupItemsFlow.trySend(items)
    }

    private fun onFiatCurrencyClicked() {
        val items = FiatCurrency.entries.map { currency ->
            val currencyCode = currency.name.uppercase()
            val currencyName = getFiatCurrencyString(currency)
            val stringBuilder = SpannableStringBuilder().append(currencyCode).append("  ").append(currencyName)
            stringBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.text_primary)), 0, currencyCode.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            stringBuilder.setSpan(ForegroundColorSpan(Res.color(RUiKitColor.text_secondary)), currencyCode.length + 1, stringBuilder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            MenuPopupWindow.MenuPopupItem(stringBuilder) { settingsRepository.setFiatCurrency(currency) }
        }
        _showFiatCurrencyPopupItemsFlow.trySend(items)
    }

    private fun onShowRecoveryClicked() {
        screenApi.navigateToPassCodeEnter(PurposeRecovery, withBiometrics = true)
    }

    private fun onChangePasscodeClicked() {
        screenApi.navigateToPassCodeEnter(PurposeChangePasscode, withBiometrics = false)
    }

    private fun getFiatCurrencyString(fiatCurrency: FiatCurrency): String {
        return when (fiatCurrency) {
            FiatCurrency.AED -> Res.str(RString.fiat_aed)
            FiatCurrency.CHF -> Res.str(RString.fiat_chf)
            FiatCurrency.CNY -> Res.str(RString.fiat_cny)
            FiatCurrency.EUR -> Res.str(RString.fiat_eur)
            FiatCurrency.GBP -> Res.str(RString.fiat_gbp)
            FiatCurrency.IDR -> Res.str(RString.fiat_idr)
            FiatCurrency.INR -> Res.str(RString.fiat_inr)
            FiatCurrency.JPY -> Res.str(RString.fiat_jpy)
            FiatCurrency.KRW -> Res.str(RString.fiat_krw)
            FiatCurrency.RUB -> Res.str(RString.fiat_rub)
            FiatCurrency.USD -> Res.str(RString.fiat_usd)
        }
    }

    companion object {

        const val ItemNotifications = 0
        const val ItemAddress = 1
        const val ItemCurrency = 2
        const val ItemRecoveryPhrase = 3
        const val ItemChangePasscode = 4
        const val ItemBiometricAuth = 5
        const val ItemDeleteWallet = 6
        const val ItemExportLogs = 100

        private const val PurposeRecovery = "recovery"
        private const val PurposeChangePasscode = "changePasscode"
        private const val PurposeChangeBiometric = "changeBiometric"

        private const val PermissionRequestIdNotifications = 10
    }
}