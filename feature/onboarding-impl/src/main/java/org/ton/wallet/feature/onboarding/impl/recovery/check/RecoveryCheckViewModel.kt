package org.ton.wallet.feature.onboarding.impl.recovery.check

import android.app.Activity
import android.os.SystemClock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.KeyboardUtils
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.domain.blockhain.api.GetRecoveryWordsUseCase
import org.ton.wallet.feature.onboarding.api.RecoveryCheckScreenApi
import org.ton.wallet.feature.onboarding.impl.base.BaseInputListViewModel
import org.ton.wallet.strings.RString
import kotlin.random.Random

class RecoveryCheckViewModel : BaseInputListViewModel() {

    private val getRecoveryWordsUseCase: GetRecoveryWordsUseCase by inject()
    private val screenApi: RecoveryCheckScreenApi by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val walletRepository: WalletRepository by inject()

    private val errorStates = BooleanArray(WordsCount)

    private val _errorStatesFlow = MutableStateFlow(errorStates)
    val errorStatesFlow: Flow<BooleanArray> = _errorStatesFlow

    private val _showErrorDialog = Channel<Unit>(Channel.BUFFERED)
    val showErrorDialog: Flow<Unit> = _showErrorDialog.receiveAsFlow()

    val subtitle: String
    val wordPositions = Array(WordsCount) { 0 }

    init {
        val wordsCount = getRecoveryWordsUseCase.wordsCount
        val range = wordsCount / wordPositions.size
        val random = Random(SystemClock.elapsedRealtime())
        for (i in wordPositions.indices) {
            wordPositions[i] = random.nextInt(range * i, range * (i + 1))
        }
        subtitle = Res.str(RString.lets_check_recovery_phrase, wordPositions[0] + 1, wordPositions[1] + 1, wordPositions[2] + 1)
    }

    override fun setEnteredWord(position: Int, word: String) {
        super.setEnteredWord(position, word)
        if (errorStates[position]) {
            val errorStates = getErrorStatesCopy()
            errorStates[position] = false
            _errorStatesFlow.tryEmit(errorStates)
        }
    }

    fun onContinueClicked(activity: Activity) {
        var hasError = false
        val errorStates = getErrorStatesCopy()
        val recoveryPhrase = walletRepository.getRecoveryPhrase()
        for (i in 0 until WordsCount) {
            errorStates[i] = recoveryPhrase[wordPositions[i]] != enteredWords[i]
            if (errorStates[i]) {
                hasError = true
            }
        }
//        if (hasError && !BuildConfig.DEBUG) {
        if (hasError) {
            _showErrorDialog.trySend(Unit)
            _errorStatesFlow.value = errorStates
        } else {
            settingsRepository.setRecoveryChecked()
            KeyboardUtils.hideKeyboard(activity.window) {
                screenApi.navigateToRecoveryFinished()
            }
        }
    }

    fun onSeeWordsClicked() {
        screenApi.navigateBack()
    }

    private fun getErrorStatesCopy(): BooleanArray {
        return errorStates.copyOf()
    }

    private companion object {
        private const val WordsCount = 3
    }
}