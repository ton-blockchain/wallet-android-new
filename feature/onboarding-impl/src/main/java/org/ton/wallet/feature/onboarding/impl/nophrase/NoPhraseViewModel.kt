package org.ton.wallet.feature.onboarding.impl.nophrase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ton.wallet.domain.wallet.api.CreateWalletUseCase
import org.ton.wallet.feature.onboarding.api.NoPhraseScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel

class NoPhraseViewModel : BaseViewModel() {

    private val screenApi: NoPhraseScreenApi by inject()
    private val createWalletUseCase: CreateWalletUseCase by inject()

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: Flow<Boolean> = _isLoadingFlow

    fun onBackClicked() {
        screenApi.navigateBack()
    }

    fun onEnterClicked() {
        screenApi.navigateBack()
    }

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
}