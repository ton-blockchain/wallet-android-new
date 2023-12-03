package org.ton.wallet.feature.send.impl.confirm

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.coreui.KeyboardUtils
import org.ton.wallet.data.core.ton.MessageData
import org.ton.wallet.data.tonclient.api.TonApiException
import org.ton.wallet.domain.transactions.api.GetSendFeeUseCase
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.send.api.SendConfirmScreenApi
import org.ton.wallet.feature.send.api.SendProcessingScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable
import org.ton.wallet.uikit.RUiKitFont

class SendConfirmViewModel(private val args: SendConfirmScreenArguments) : BaseViewModel() {

    private val getSendFeeUseCase: GetSendFeeUseCase by inject()
    private val screenApi: SendConfirmScreenApi by inject()
    private val snackBarController: SnackBarController by inject()

    private val recipientAddress = args.address
    private val sourceAmount = args.amount
    private var calculateFeeJob: Job? = null
    private var passCodeEntered = false

    private val _amountFlow = MutableStateFlow(sourceAmount)
    private val _isFeeLoadingFlow = MutableStateFlow(false)
    private val _feeFlow = MutableStateFlow(-1L)
    private val _messageFlow = MutableStateFlow("")

    val amountStringFlow: Flow<String> = _amountFlow.map(Formatter::getFormattedAmount)

    val feeStateFlow: Flow<SendConfirmFeeState> = combine(_feeFlow, _isFeeLoadingFlow) { fee, isLoading ->
        if (isLoading) {
            SendConfirmFeeState.Loading
        } else if (fee < 0) {
            SendConfirmFeeState.Error
        } else {
            SendConfirmFeeState.Value(Formatter.getFormattedAmount(fee))
        }
    }

    val messageLeftSymbolsFlow: StateFlow<Int> = _messageFlow.map { MessageMaxLength - it.length }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MessageMaxLength)

    val recipient = Formatter.getBeautifiedShortString(Formatter.getShortAddress(recipientAddress), Res.font(RUiKitFont.roboto_regular))

    val presetMessage: String? = args.message

    init {
        _messageFlow.debounce(300L)
            .drop(1)
            .onEach(::calculateFee)
            .launchIn(viewModelScope)
        calculateFee(_messageFlow.value)
    }

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        if (!isStarted && !isPush && isEnter && passCodeEntered) {
            val message = _messageFlow.value.ifEmpty { null }
            screenApi.navigateToSendProcessing(recipientAddress, _amountFlow.value, _feeFlow.value, message)
            passCodeEntered = false
        }
    }

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == PassCodeEnterScreenApi.ResultKeyPassCodeEntered) {
            val purpose = args?.getString(PassCodeEnterScreenApi.ArgumentKeyPurpose)
            if (purpose == PassCodeEnterPurposeConfirmSend) {
                passCodeEntered = true
                screenApi.navigateBack()
            }
        } else if (code == SendProcessingScreenApi.ResultKeyFeeChanged) {
            calculateFee(_messageFlow.value)
        }
    }

    fun onTextChanged(text: String) {
        _messageFlow.value = text
    }

    fun onConfirmClicked(activity: Activity) {
        if (_feeFlow.value < 0 || messageLeftSymbolsFlow.value < 0) {
            return
        }
        passCodeEntered = false
        KeyboardUtils.hideKeyboard(activity.window) {
            screenApi.navigateToPassCodeEnter(PassCodeEnterPurposeConfirmSend)
        }
    }

    private fun calculateFee(message: String) {
        calculateFeeJob?.cancel()
        _isFeeLoadingFlow.tryEmit(true)
        calculateFeeJob = viewModelScope.launch(Dispatchers.IO) {
            val amount = _amountFlow.value
            try {
                val messages = listOf(MessageData.text(destination = recipientAddress, amount = amount, message))
                val fee = getSendFeeUseCase.invoke(messages)
                _feeFlow.emit(fee)
            } catch (e: TonApiException) {
                if (e.message == "NOT_ENOUGH_FUNDS") {
                    var zeroAmountFee: Long? = null
                    try {
                        val messages = listOf(MessageData.text(destination = recipientAddress, amount = 0, message))
                        zeroAmountFee = getSendFeeUseCase.invoke(messages)
                    } catch (e: TonApiException) {
                        if (e.message == "NOT_ENOUGH_FUNDS") {
                            showNotEnoughFundsError()
                            _feeFlow.emit(-1)
                        } else {
                            throw e
                        }
                    }

                    if (zeroAmountFee != null) {
                        val decreasedAmount = amount - zeroAmountFee
                        try {
                            val messages = listOf(MessageData.text(destination = recipientAddress, amount = decreasedAmount, message))
                            val decreasedAmountFee = getSendFeeUseCase.invoke(messages)
                            _feeFlow.emit(decreasedAmountFee)
                            _amountFlow.emit(decreasedAmount)
                            snackBarController.showMessage(SnackBarMessage(
                                title = null,
                                message = Res.str(RString.decreased_amount),
                                drawable = Res.drawable(RUiKitDrawable.ic_warning_32),
                            ))
                        } catch (e: TonApiException) {
                            if (e.message == "NOT_ENOUGH_FUNDS") {
                                showNotEnoughFundsError()
                                _feeFlow.emit(-1)
                            } else {
                                throw e
                            }
                        }
                    }
                } else {
                    throw e
                }
            } finally {
                _isFeeLoadingFlow.emit(false)
            }
        }
    }

    private fun showNotEnoughFundsError() {
        snackBarController.showMessage(SnackBarMessage(
            title = null,
            message = Res.str(RString.not_enough_funds),
            drawable = Res.drawable(RUiKitDrawable.ic_warning_32),
            durationMs = 5000L
        ))
    }

    private companion object {
        private const val MessageMaxLength = 122
        private const val PassCodeEnterPurposeConfirmSend = "confirmSend"
    }
}