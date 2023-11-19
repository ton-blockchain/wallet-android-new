package org.ton.wallet.feature.tonconnect.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.wallet.core.ext.toUriSafe
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.link.LinkUtils
import org.ton.wallet.data.core.model.TonAccountType
import org.ton.wallet.domain.tonconnect.api.TonConnectOpenConnectionUseCase
import org.ton.wallet.domain.wallet.api.GetCurrentAccountDataUseCase
import org.ton.wallet.feature.tonconnect.api.TonConnectApproveScreenApi
import org.ton.wallet.lib.tonconnect.TonConnectApi
import org.ton.wallet.lib.tonconnect.TonConnectClient
import org.ton.wallet.screen.viewmodel.BaseViewModel

class TonConnectApproveViewModel(args: TonConnectApproveScreenArguments) : BaseViewModel() {

    private val getCurrentAccountUseCase: GetCurrentAccountDataUseCase by inject()
    private val tonConnectOpenConnectionUseCase: TonConnectOpenConnectionUseCase by inject()
    private val screenApi: TonConnectApproveScreenApi by inject()
    private val tonConnectClient: TonConnectClient by inject()

    private var action: LinkAction.TonConnectAction? = null
    private var manifest: TonConnectApi.AppManifest? = null

    private val _stateFlow = MutableStateFlow(TonConnectApproveState(true))
    val stateFlow: Flow<TonConnectApproveState> = _stateFlow

    init {
        viewModelScope.launch(Dispatchers.IO) {
            action = LinkUtils.parseLink(args.url) as? LinkAction.TonConnectAction
                ?: throw IllegalArgumentException("Invalid action ${args.url}")
            val manifestUrl = action!!.request.manifestUrl
            val account = getCurrentAccountUseCase.getAccountState()
                ?: throw IllegalArgumentException("Current account is null")
            val accountType = TonAccountType.getAccountType(account.version, account.revision)
            try {
                val manifest = tonConnectClient.getManifest(manifestUrl)
                val state = TonConnectApproveState(
                    isDataLoading = false,
                    accountAddress = account.address,
                    accountVersion = accountType.getString(),
                    appName = manifest.name,
                    appIconUrl = manifest.iconUrl,
                    appHost = manifest.url.toUriSafe()?.host ?: manifest.name,
                    connectionState = TonConnectApproveState.ConnectionDefault,
                )
                this@TonConnectApproveViewModel.manifest = manifest
                _stateFlow.tryEmit(state)
            } catch (e: Exception) {
                screenApi.navigateBack()
                throw e
            }
        }
    }

    fun onCloseClicked() {
        screenApi.navigateBack()
    }

    fun onConnectClicked() {
        val action = action ?: return
        _stateFlow.value = _stateFlow.value.copy(connectionState = TonConnectApproveState.ConnectionInProgress)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tonConnectOpenConnectionUseCase.invoke(action, manifest!!)
                _stateFlow.value = _stateFlow.value.copy(connectionState = TonConnectApproveState.ConnectionConnected)
                withContext(Dispatchers.Main) {
                    delay(1000L)
                    screenApi.navigateBack()
                }
            } catch (e: Exception) {
                _stateFlow.value = _stateFlow.value.copy(connectionState = TonConnectApproveState.ConnectionDefault)
                throw e
            }
        }
    }
}