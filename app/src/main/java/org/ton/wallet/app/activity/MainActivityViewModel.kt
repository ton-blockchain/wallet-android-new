package org.ton.wallet.app.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ton.wallet.app.action.TonConnectEventHandler
import org.ton.wallet.app.navigation.BackStackItem
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.*
import org.ton.wallet.core.ThreadUtils
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.data.core.link.LinkActionHandler
import org.ton.wallet.data.core.link.LinkUtils
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.feature.onboarding.impl.recovery.finished.RecoveryFinishedScreenArguments
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.passcode.impl.enter.PassCodeEnterScreenArguments
import org.ton.wallet.lib.security.BiometricUtils
import org.ton.wallet.lib.tonconnect.TonConnectClient
import org.ton.wallet.lib.tonconnect.TonConnectEvent
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.screen.viewmodel.ActivityViewModel

class MainActivityViewModel : ActivityViewModel() {

    private val authRepository: AuthRepository by inject()
    private val context: Context by inject()
    private val linkActionHandler: LinkActionHandler by inject()
    private val navigator: Navigator by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val tonConnectClient: TonConnectClient by inject()
    private val tonConnectEventHandler: TonConnectEventHandler by inject()
    private val walletRepository: WalletRepository by inject()

    private var savedBackStack: List<BackStackItem>? = null
    private var intent: Intent? = null
    private var isPassCodeShown = false

    private val isPasscodeAtTop
        get() = navigator.topScreenTag == AppScreen.PassCodeEnter.name

    val isNeedShowPassCode: Boolean
        get() = walletRepository.hasWalletFlow.value && authRepository.hasPassCode

    init {
        AppLifecycleDetector.isAppForegroundFlow
            .onEach { isForeground ->
                if (isForeground) {
                    showAppPassCode()
                    AppIntentUtils.isAppIntentStarted = false
                } else {
                    isPassCodeShown = false
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.Main) {
            AppBrowserUtils.init(context)
        }
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == PassCodeEnterScreenApi.ResultKeyPassCodeEntered) {
            val purpose = args?.getString(PassCodeEnterScreenApi.ArgumentKeyPurpose)
            if (purpose == PurposeAppUnlock) {
                val backStack = savedBackStack
                if (backStack == null) {
                    navigateToWallet()
                } else {
                    navigator.setBackStack(backStack)
                }
                ThreadUtils.postOnMain(::handleIntent, 500L)
            }
        }
    }

    override fun onActivityResumed() {
        super.onActivityResumed()
        authRepository.setBiometricAvailableOnDevice(BiometricUtils.isBiometricsAvailableOnDevice(context))
        settingsRepository.setNotificationsPermission(isNotificationsAllowed())
    }

    fun onAnimationFinished() {
        if (walletRepository.hasWalletFlow.value) {
            performNavigateWithCreatedWallet()
        } else {
            performNavigateOnboarding()
        }
    }

    fun setIntent(intent: Intent?) {
        this.intent = intent
        ThreadUtils.postOnMain {
            if (AppLifecycleDetector.isAppForegroundFlow.value && !isPasscodeAtTop && isPassCodeShown) {
                handleIntent()
            }
        }
    }

    private fun handleIntent() {
        if (intent == null) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val tonConnectEvent = intent?.getParcelableExtra<TonConnectEvent>(MainActivity.ArgumentKeyTonConnectAction)
            val action = LinkUtils.parseLink(intent?.data?.toString() ?: "")
            if (tonConnectEvent != null) {
                tonConnectEventHandler.onTonConnectEvent(tonConnectEvent)
            } else if (action != null) {
                linkActionHandler.processLinkAction(action)
            }
            intent = null
        }
    }

    private fun showAppPassCode() {
        val backStack = navigator.getBackStack()
        if (!AppIntentUtils.isAppIntentStarted
            && authRepository.hasPassCode
            && !isPasscodeAtTop
            && backStack.isNotEmpty()
        ) {
            savedBackStack = backStack
            navigator.push(PassCodeEnterScreenArguments(purpose = PurposeAppUnlock, isDark = true), isRoot = true, isAnimated = false)
        }
        isPassCodeShown = true
    }

    private fun navigateToWallet() {
        navigator.push(screen = AppScreen.Main.name, isRoot = true, isReplace = true)
        CoroutineScopes.appScope.launch(Dispatchers.IO) {
            tonConnectClient.restoreConnections()
        }
    }

    private fun isNotificationsAllowed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun performNavigateOnboarding() {
        navigator.push(AppScreen.OnboardingStart.name, isRoot = true, isAnimated = false)
    }

    private fun performNavigateWithCreatedWallet() {
        if (authRepository.hasPassCode) {
            navigator.push(PassCodeEnterScreenArguments(purpose = PurposeAppUnlock, isDark = true), isRoot = true, isAnimated = false)
        } else if (settingsRepository.isRecoveryChecked) {
            navigator.push(RecoveryFinishedScreenArguments(false), isRoot = true, isAnimated = false)
        } else {
            navigator.push(AppScreen.OnboardingCongratulations.name, isRoot = true, isAnimated = false)
        }
    }

    private companion object {

        private const val PurposeAppUnlock = "appUnlock"
    }
}