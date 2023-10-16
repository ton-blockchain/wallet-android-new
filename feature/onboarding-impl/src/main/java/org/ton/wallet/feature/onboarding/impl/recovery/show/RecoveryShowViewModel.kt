package org.ton.wallet.feature.onboarding.impl.recovery.show

import android.os.SystemClock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.ton.wallet.data.wallet.api.WalletRepository
import org.ton.wallet.feature.onboarding.api.RecoveryShowScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel

class RecoveryShowViewModel(
    private val args: RecoveryShowScreenArguments
) : BaseViewModel() {

    private val screenApi: RecoveryShowScreenApi by inject()
    private val walletRepository: WalletRepository by inject()

    private var showTime = SystemClock.elapsedRealtime()
    private var doneClicksCount = 0

    private val _showAlertFlow = Channel<Boolean>(Channel.BUFFERED)
    val showAlertFlow: Flow<Boolean> = _showAlertFlow.receiveAsFlow()

    val wordsFlow: Flow<List<String>> = flowOf(walletRepository.getRecoveryPhrase())

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        val popToScreen = args.popToScreenName
        if (popToScreen != null && !isStarted && isPush && isEnter) {
            screenApi.popToScreenKeepCurrent(popToScreen)
        }
    }

    fun onDoneClicked() {
        if (args.isOnlyShow) {
            screenApi.navigateBack()
            return
        }

        doneClicksCount++
        // if (!BuildConfig.DEBUG && SystemClock.elapsedRealtime() - showTime < 60 * 1000) {
        if (SystemClock.elapsedRealtime() - showTime < 60 * 1000) {
            _showAlertFlow.trySend(doneClicksCount > 1)
        } else {
            screenApi.navigateToCheck()
        }
    }

    fun onSkipClicked() {
        screenApi.navigateToCheck()
    }
}