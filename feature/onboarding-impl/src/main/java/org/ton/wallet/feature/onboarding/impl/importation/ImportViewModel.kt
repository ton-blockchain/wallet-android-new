package org.ton.wallet.feature.onboarding.impl.importation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ton.wallet.data.tonclient.api.TonApiException
import org.ton.wallet.domain.wallet.api.CreateWalletUseCase
import org.ton.wallet.feature.onboarding.api.ImportScreenApi
import org.ton.wallet.feature.onboarding.impl.base.BaseInputListViewModel

class ImportViewModel : BaseInputListViewModel() {

    private val screenApi: ImportScreenApi by inject()
    private val createWalletUseCase: CreateWalletUseCase by inject()

    private val _showIncorrectWordsAlert = Channel<Unit>(Channel.BUFFERED)
    val showIncorrectWordsDialog: Flow<Unit> = _showIncorrectWordsAlert.receiveAsFlow()

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: Flow<Boolean> = _isLoadingFlow

    fun onDoneClicked() {
        _isLoadingFlow.tryEmit(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                createWalletUseCase.invoke(enteredWords)
                screenApi.navigateToRecoveryChecked()
            } catch (e: Exception) {
                if (e is TonApiException && e.message?.startsWith("INVALID_MNEMONIC") == true) {
                    _showIncorrectWordsAlert.trySend(Unit)
                } else {
                    throw e
                }
            } finally {
                _isLoadingFlow.tryEmit(false)
            }
        }
    }

    fun onNoPhraseClicked() {
        screenApi.navigateToNoPhrase()
    }
}