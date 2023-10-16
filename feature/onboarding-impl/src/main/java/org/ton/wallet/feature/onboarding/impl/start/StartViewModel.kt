package org.ton.wallet.feature.onboarding.impl.start

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ton.wallet.domain.wallet.api.CreateWalletUseCase
import org.ton.wallet.feature.onboarding.api.StartScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel

class StartViewModel : BaseViewModel() {

    private val screenApi by inject<StartScreenApi>()
    private val createWalletUseCase: CreateWalletUseCase by inject()

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: Flow<Boolean> = _isLoadingFlow

    fun onCreateClicked() {
        _isLoadingFlow.tryEmit(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                createWalletUseCase.invoke(null)
                screenApi.navigateToCongratulations()
            } finally {
                _isLoadingFlow.tryEmit(false)
            }
        }
    }

    fun onImportClicked() {
        screenApi.navigateToImport()
    }
}