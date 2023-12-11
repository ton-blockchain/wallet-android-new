package org.ton.wallet.feature.wallet.impl.main

import android.os.Bundle
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.link.LinkActionHandler
import org.ton.wallet.data.settings.api.NetworkStateRepository
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.transactions.api.TransactionsRepository
import org.ton.wallet.domain.transactions.api.GetTransactionsUseCase
import org.ton.wallet.domain.transactions.api.model.TransactionDataUiListItem
import org.ton.wallet.domain.wallet.api.GetCurrentAccountBalanceUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.feature.scanqr.api.ScanQrScreenApi
import org.ton.wallet.feature.wallet.api.MainScreenApi
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.viewmodel.BaseViewModel

class MainScreenViewModel : BaseViewModel() {

    private val getCurrentAccountBalanceUseCase: GetCurrentAccountBalanceUseCase by inject()
    private val getCurrentAccountDataUseCase: GetCurrentAccountDataUseCase by inject()
    private val getTransactionsUseCase: GetTransactionsUseCase by inject()
    private val linkActionHandler: LinkActionHandler by inject()
    private val networkStateRepository: NetworkStateRepository by inject()
    private val screenApi: MainScreenApi by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val transactionsRepository: TransactionsRepository by inject()

    private val isAccountStateRefreshing = MutableStateFlow(false)
    private var refreshAccountJob: Job? = null
    private var linkAction: LinkAction? = null

    val headerStateFlow: Flow<MainScreenState> = combine(
        networkStateRepository.networkStateFlow,
        isAccountStateRefreshing,
        getCurrentAccountDataUseCase.getAddressFlow(),
        getCurrentAccountBalanceUseCase.getTonBalanceFlow(),
        getCurrentAccountBalanceUseCase.getFiatBalanceStringFlow()
    ) { networkState, isRefreshing, address, tonBalance, fiatBalanceString ->
        MainScreenState(
            address = address,
            tonBalance = tonBalance,
            fiatBalanceString = fiatBalanceString,
            headerState = mapHeaderStatus(networkState.isAvailable, isRefreshing)
        )
    }

    private val _transactionsFlow = MutableStateFlow<List<Any>?>(null)
    val transactionsFlow: Flow<List<Any>?> = _transactionsFlow

    private val _showNotificationPermissionFlow = Channel<Unit>(Channel.BUFFERED)
    val showNotificationPermissionFlow: Flow<Unit> = _showNotificationPermissionFlow.receiveAsFlow()

    init {
        networkStateRepository.networkStateFlow
            .drop(1)
            .filter { it.isAvailable }
            .onEach { refreshTransactions() }
            .launchIn(viewModelScope + Dispatchers.IO)

        getCurrentAccountDataUseCase.getAccountStateFlow()
            .distinctUntilChanged()
            .onEach {
                _transactionsFlow.value = null
                loadCachedTransactions()
                refreshTransactions()
            }
            .launchIn(viewModelScope + Dispatchers.IO)

        transactionsRepository.transactionsAddedFlow
            .filterNotNull()
            .onEach { loadCachedTransactions() }
            .launchIn(viewModelScope + Dispatchers.IO)

        viewModelScope.launch(Dispatchers.Main) {
            if (!settingsRepository.hasNotificationsPermissionFlow.value && !settingsRepository.isNotificationPermissionDialogShown) {
                _showNotificationPermissionFlow.send(Unit)
                settingsRepository.setNotificationsDialogShown()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _transactionsFlow.value = null
            loadCachedTransactions()
            refreshTransactions()
        }
    }

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        if (!isStarted && isEnter && !isPush) {
            linkAction?.let(linkActionHandler::processLinkAction)
            linkAction = null
        }
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == ScanQrScreenApi.ResultCodeQrDetected) {
            processLinkAction(args?.getParcelable(ScanQrScreenApi.ArgumentKeyLinkAction) as? LinkAction)
        }
    }

    fun onScanClicked() {
        screenApi.navigateToScanQr()
    }

    fun onSettingsClicked() {
        screenApi.navigateToSettings()
    }

    fun onSendClicked() {
        screenApi.navigateToSend()
    }

    fun onReceiveClicked() {
        screenApi.navigateToReceive()
    }

    fun onTransactionClicked(item: TransactionDataUiListItem) {
        screenApi.navigateToTransactionsDetails(item.internalId, item.isMultiMessage)
    }

    fun onRefresh() {
        refreshTransactions()
    }

    private suspend fun loadCachedTransactions() {
        try {
            _transactionsFlow.value = getTransactionsUseCase.invoke(false)
        } catch (e: Exception) {
            L.e(e)
        }
    }

    private fun refreshTransactions() {
        if (refreshAccountJob?.isActive == true) {
            return
        }
        isAccountStateRefreshing.tryEmit(true)
        refreshAccountJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionsFlow.value = getTransactionsUseCase.invoke(true)
            } finally {
                isAccountStateRefreshing.tryEmit(false)
            }
        }
    }

    private fun mapHeaderStatus(isNetworkAvailable: Boolean, isRefreshing: Boolean): MainScreenHeaderState {
        return if (isNetworkAvailable) {
            if (isRefreshing) {
                MainScreenHeaderState.Updating
            } else {
                MainScreenHeaderState.Default
            }
        } else {
            MainScreenHeaderState.WaitingNetwork
        }
    }

    private fun processLinkAction(linkAction: LinkAction?) {
        if (this.linkAction != null) {
            return
        }
        if (linkAction is LinkAction.TransferAction || linkAction is LinkAction.TonConnectAction) {
            this.linkAction = linkAction
            screenApi.navigateToMain()
        }
    }
}