package org.ton.wallet.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.ton.wallet.app.action.*
import org.ton.wallet.app.data.AppDataBase
import org.ton.wallet.app.navigation.ConductorNavigator
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.screen.*
import org.ton.wallet.app.util.AppFiles
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.data.auth.impl.AuthRepositoryImpl
import org.ton.wallet.data.core.link.LinkActionHandler
import org.ton.wallet.data.notifications.api.NotificationsRepository
import org.ton.wallet.data.notifications.impl.NotificationsRepositoryImpl
import org.ton.wallet.data.prices.api.PricesRepository
import org.ton.wallet.data.prices.impl.*
import org.ton.wallet.data.prices.impl.dao.FiatPricesDao
import org.ton.wallet.data.prices.impl.dao.FiatPricesDaoImpl
import org.ton.wallet.data.settings.api.NetworkStateRepository
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.settings.impl.NetworkStateRepositoryImpl
import org.ton.wallet.data.settings.impl.SettingsRepositoryImpl
import org.ton.wallet.data.tonclient.api.TonClient
import org.ton.wallet.data.tonclient.impl.TonClientImpl
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.data.transactions.impl.*
import org.ton.wallet.data.wallet.api.*
import org.ton.wallet.data.wallet.impl.AccountsRepositoryImpl
import org.ton.wallet.data.wallet.impl.WalletRepositoryImpl
import org.ton.wallet.data.wallet.impl.dao.AccountsDaoImpl
import org.ton.wallet.di.DiScope
import org.ton.wallet.domain.blockhain.api.*
import org.ton.wallet.domain.blockhain.impl.*
import org.ton.wallet.domain.settings.api.DeleteWalletUseCase
import org.ton.wallet.domain.settings.impl.DeleteWalletUseCaseImpl
import org.ton.wallet.domain.tonconnect.api.TonConnectOpenConnectionUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectSendResponseUseCase
import org.ton.wallet.domain.tonconnect.impl.TonConnectOpenConnectionUseCaseImpl
import org.ton.wallet.domain.tonconnect.impl.TonConnectSendResponseUseCaseImpl
import org.ton.wallet.domain.transactions.api.*
import org.ton.wallet.domain.transactions.impl.*
import org.ton.wallet.domain.wallet.api.*
import org.ton.wallet.domain.wallet.impl.*
import org.ton.wallet.feature.onboarding.api.*
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.passcode.api.PassCodeSetupScreenApi
import org.ton.wallet.feature.scanqr.api.ScanQrScreenApi
import org.ton.wallet.feature.send.api.*
import org.ton.wallet.feature.settings.api.SettingsScreenApi
import org.ton.wallet.feature.tonconnect.api.TonConnectApproveScreenApi
import org.ton.wallet.feature.transactions.api.TransactionDetailsScreenApi
import org.ton.wallet.feature.wallet.api.MainScreenApi
import org.ton.wallet.feature.wallet.api.ReceiveScreenApi
import org.ton.wallet.lib.sqlite.SqliteDatabase
import org.ton.wallet.lib.tonconnect.TonConnectClient
import org.ton.wallet.screen.viewmodel.ViewModelDiScopeProvider
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarControllerImpl
import org.ton.wallet.uicomponents.util.ClipboardController
import org.ton.wallet.uicomponents.util.ClipboardControllerImpl
import java.io.File

object Injector {

    const val DefaultSharedPreferences = "defaultPrefs"
    const val SecuredSharedPreferences = "securedPrefs"

    private lateinit var application: Application

    val appDiScope: DiScope by lazy {
        DiScope {
            singleton<Context> { application }

            // app stuff
            singleton<Navigator> { ConductorNavigator() }
            singleton<SnackBarController> { SnackBarControllerImpl() }
            singleton<ClipboardController> {
                ClipboardControllerImpl(
                    context = getInstance(),
                    snackBarController = getInstance()
                )
            }

            // data
            singleton<Json> {
                Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            }
            singleton<SharedPreferences>(DefaultSharedPreferences) {
                application.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            }
            singleton<SharedPreferences>(SecuredSharedPreferences) {
                EncryptedSharedPreferences.create(
                    "a",
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    application,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            singleton<OkHttpClient>() {
                val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            }
            singleton<TonClient> {
                TonClientImpl(
                    sharedPreferences = getInstance(DefaultSharedPreferences),
                    okHttpClient = getInstance(),
                    keysDirectory = File(AppFiles.getFilesDir(), "ton0")
                )
            }

            singleton<SqliteDatabase> { AppDataBase(getInstance()) }

            // api
            singleton<PricesApi> {
                PricesApiImpl(okHttpClient = getInstance())
            }
            singleton<TonConnectClient> {
                TonConnectClient.createInstance(
                    json = getInstance(),
                    sharedPreferences = getInstance(DefaultSharedPreferences),
                    securedPreferences = getInstance(SecuredSharedPreferences)
                )
            }

            // dao
            singleton<AccountsDao> { AccountsDaoImpl(db = getInstance()) }
            singleton<FiatPricesDao> { FiatPricesDaoImpl(db = getInstance()) }
            singleton<TransactionsDao> { TransactionsDaoImpl(db = getInstance()) }

            // repositories
            singleton<AccountsRepository> {
                AccountsRepositoryImpl(
                    tonClient = getInstance(),
                    accountsDao = getInstance(),
                    securedPreferences = getInstance(SecuredSharedPreferences)
                )
            }
            singleton<AuthRepository> {
                AuthRepositoryImpl(securedPreferences = getInstance(SecuredSharedPreferences))
            }
            singleton<NetworkStateRepository> {
                NetworkStateRepositoryImpl(context = getInstance())
            }
            singleton<NotificationsRepository> {
                NotificationsRepositoryImpl(channelIdPrefix = BuildConfig.APPLICATION_ID)
            }
            singleton<PricesRepository> {
                PricesRepositoryImpl(
                    pricesApi = getInstance(),
                    pricesDao = getInstance()
                )
            }
            singleton<SettingsRepository> {
                SettingsRepositoryImpl(preferences = getInstance(DefaultSharedPreferences))
            }
            singleton<TransactionsRepository> {
                TransactionsRepositoryImpl(
                    tonClient = getInstance(),
                    transactionsDao = getInstance(),
                )
            }
            singleton<WalletRepository> {
                WalletRepositoryImpl(
                    tonClient = getInstance(),
                    defaultPreferences = getInstance(DefaultSharedPreferences),
                    securedPreferences = getInstance(SecuredSharedPreferences),
                )
            }

            // domain
            factory<CreateWalletUseCase> {
                CreateWalletUseCaseImpl(
                    accountsRepository = getInstance(),
                    settingsRepository = getInstance(),
                    walletRepository = getInstance(),
                )
            }
            factory<DeleteWalletUseCase> {
                val repositories = listOf(
                    getInstance<AccountsRepository>(),
                    getInstance<AuthRepository>(),
                    getInstance<NetworkStateRepository>(),
                    getInstance<NotificationsRepository>(),
                    getInstance<PricesRepository>(),
                    getInstance<SettingsRepository>(),
                    getInstance<TransactionsRepository>(),
                    getInstance<WalletRepository>()
                )
                DeleteWalletUseCaseImpl(
                    defaultPreferences = getInstance(DefaultSharedPreferences),
                    securedPreferences = getInstance(SecuredSharedPreferences),
                    repositories = repositories
                )
            }
            factory<GetAddressTypeUseCase> {
                GetAddressTypeUseCaseImpl(
                    getAddressUseCase = getInstance()
                )
            }
            factory<GetAddressUseCase> {
                GetAddressUseCaseImpl(
                    tonClient = getInstance()
                )
            }
            singleton<GetCurrentAccountDataUseCase> {
                GetCurrentAccountDataUseCaseImpl(
                    accountsRepository = getInstance(),
                    settingsRepository = getInstance()
                )
            }
            factory<GetCurrentAccountBalanceUseCase> {
                GetCurrentAccountBalanceUseCaseImpl(
                    getCurrentAccountDataUseCase = getInstance(),
                    pricesRepository = getInstance(),
                    settingsRepository = getInstance()
                )
            }
            factory<GetRecentSendTransactionsUseCase> {
                GetRecentSendTransactionsUseCaseImpl(
                    accountsRepository = getInstance(),
                    settingsRepository = getInstance(),
                    transactionsRepository = getInstance()
                )
            }
            factory<GetRecoveryWordsUseCase> {
                GetRecoveryHintsUseCaseImpl(tonClient = getInstance())
            }
            factory<GetSendFeeUseCase> {
                GetSendFeeUseCaseImpl(
                    getCurrentAccountDataUseCase = getInstance(),
                    transactionsRepository = getInstance(),
                    walletRepository = getInstance()
                )
            }
            factory<GetTransactionDetailsUseCase> {
                GetTransactionDetailsUseCaseImpl(
                    transactionsRepository = getInstance()
                )
            }
            factory<GetTransactionsUseCase> {
                GetTransactionsUseCaseImpl(
                    accountsRepository = getInstance(),
                    transactionsRepository = getInstance(),
                    settingsRepository = getInstance()
                )
            }
            singleton<RefreshCurrentAccountStateUseCase> {
                RefreshCurrentAccountStateUseCaseImpl(
                    accountsRepository = getInstance(),
                    settingsRepository = getInstance()
                )
            }
            factory<SendUseCase> {
                SendUseCaseImpl(
                    getCurrentAccountDataUseCase = getInstance(),
                    transactionsRepository = getInstance(),
                    walletRepository = getInstance()
                )
            }
            factory<TonConnectOpenConnectionUseCase> {
                TonConnectOpenConnectionUseCaseImpl(
                    appVersion = BuildConfig.VERSION_NAME,
                    json = getInstance(),
                    tonConnectClient = getInstance(),
                    walletRepository = getInstance(),
                    getAddressUseCase = getInstance(),
                    getCurrentAccountDataUseCase = getInstance()
                )
            }
            factory<TonConnectSendResponseUseCase> {
                TonConnectSendResponseUseCaseImpl(
                    json = getInstance(),
                    tonConnectClient = getInstance()
                )
            }

            // other
            singleton<LinkActionHandler> {
                LinkActionHandlerImpl(
                    navigator = getInstance(),
                    snackBarController = getInstance(),
                    getAddressTypeUseCase = getInstance()
                )
            }
            singleton<TonConnectEventHandler> {
                TonConnectEventHandlerImpl(
                    navigator = getInstance(),
                    notificationsRepository = getInstance(),
                    settingsRepository = getInstance(),
                    tonConnectClient = getInstance()
                )
            }
        }
    }

    private val viewModelDiScope: DiScope by lazy {
        DiScope(listOf(appDiScope)) {
            factory<StartScreenApi> { StartScreenApiImpl(getInstance()) }
            factory<ImportScreenApi> { ImportScreenApiImpl(getInstance()) }
            factory<NoPhraseScreenApi> { NoPhraseScreenApiImpl(getInstance()) }
            factory<RecoveryFinishedScreenApi> { RecoveryFinishedScreenApiImpl(getInstance()) }
            factory<CongratulationsScreenApi> { CongratulationsScreenApiImpl(getInstance()) }
            factory<RecoveryShowScreenApi> { RecoveryShowScreenApiImpl(getInstance()) }
            factory<RecoveryCheckScreenApi> { RecoveryCheckScreenApiImpl(getInstance()) }
            factory<PassCodeEnterScreenApi> { PassCodeEnterScreenApiImpl(getInstance()) }
            factory<PassCodeSetupScreenApi> { PassCodeSetupScreenApiImpl(getInstance()) }
            factory<OnboardingFinishedScreenApi> { OnboardingFinishedScreenApiImpl(getInstance()) }
            factory<MainScreenApi> { MainScreenApiImpl(getInstance()) }
            factory<ReceiveScreenApi> { ReceiveScreenApiImpl(getInstance()) }
            factory<ScanQrScreenApi> { ScanQrScreenApiImpl(getInstance()) }
            factory<SettingsScreenApi> { SettingsScreenApiImpl(getInstance()) }
            factory<SendAddressScreenApi> { SendAddressScreenApiImpl(getInstance()) }
            factory<SendAmountScreenApi> { SendAmountScreenApiImpl(getInstance()) }
            factory<SendConfirmScreenApi> { SendConfirmScreenApiImpl(getInstance()) }
            factory<SendConnectConfirmScreenApi> { SendConnectConfirmScreenApiImpl(getInstance()) }
            factory<SendProcessingScreenApi> { SendProcessingScreenApiImpl(getInstance()) }
            factory<TonConnectApproveScreenApi> { TonConnectApproveScreenApiImpl(getInstance()) }
            factory<TransactionDetailsScreenApi> { TransactionDetailsScreenApiImpl(getInstance()) }
        }
    }

    init {
        ViewModelDiScopeProvider.init { viewModelDiScope }
    }

    fun setApplication(application: Application) {
        this.application = application
    }
}