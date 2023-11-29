package org.ton.wallet.feature.send.impl.processing

import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.domain.transactions.api.GetSendFeeUseCase
import org.ton.wallet.domain.transactions.api.SendUseCase
import org.ton.wallet.feature.send.api.SendProcessingScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable
import kotlin.math.abs

class SendProcessingViewModel(private val args: SendProcessingScreenArguments) : BaseViewModel() {

    private val getSendFeeUseCase: GetSendFeeUseCase by inject()
    private val screenApi: SendProcessingScreenApi by inject()
    private val snackBarController: SnackBarController by inject()
    private val sendUseCase: SendUseCase by inject()

    private val presetFee = args.fee
    private var isPoppedToMain = false

    val address = args.address

    private val _amountFlow = MutableStateFlow(args.amount)
    val amountTextFlow: Flow<String> = _amountFlow.map { amount ->
        val amountText = Formatter.getFormattedAmount(amount)
        Res.str(RString.toncoin_have_been_sent, amountText)
    }

    private val _showCompletedFlow = Channel<Unit>(Channel.BUFFERED)
    val showCompletedFlow: Flow<Unit> = _showCompletedFlow.receiveAsFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messages = listOf(MessageData.text(destination = address, amount = _amountFlow.value, text = args.message ?: ""))
                val actualFee = getSendFeeUseCase.invoke(messages)
                val feeDiff = abs(actualFee - presetFee)
                if (feeDiff.toDouble() / presetFee > (0.01 * 1e9)) {
                    snackBarController.showMessage(SnackBarMessage(
                        title = Res.str(RString.fee_changed),
                        message = Res.str(RString.fee_changed_check),
                        drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                    ))
                    setResult(SendProcessingScreenApi.ResultKeyFeeChanged, Bundle.EMPTY)
                    screenApi.navigateBack()
                    return@launch
                }

                val sendResult = sendUseCase.invoke(messages)
                _amountFlow.value = sendResult.amount
                onSendCompleted()
            } catch (e: Exception) {
                screenApi.navigateBack()
                throw e
            }
        }
    }

    fun onCloseClicked() {
        screenApi.navigateBack()
    }

    fun onViewWalletClicked() {
        popUntilMain()
        screenApi.navigateBack()
    }

    private fun onSendCompleted() {
        _showCompletedFlow.trySend(Unit)
        popUntilMain()
    }

    private fun popUntilMain() {
        if (!isPoppedToMain) {
            screenApi.navigateToMain()
        }
        isPoppedToMain = true
    }
}