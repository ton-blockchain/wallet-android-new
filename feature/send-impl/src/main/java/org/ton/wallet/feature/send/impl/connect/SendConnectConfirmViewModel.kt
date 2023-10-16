package org.ton.wallet.feature.send.impl.connect

import android.os.Bundle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.Formatter
import org.ton.wallet.data.core.connect.TonConnect
import org.ton.wallet.data.core.util.CoroutineScopes
import org.ton.wallet.domain.blockhain.api.GetAddressUseCase
import org.ton.wallet.domain.tonconnect.api.TonConnectSendUseCase
import org.ton.wallet.domain.transactions.api.GetSendFeeUseCase
import org.ton.wallet.domain.transactions.api.SendUseCase
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.send.api.SendConnectConfirmScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable

class SendConnectConfirmViewModel(private val args: SendConnectConfirmScreenArguments) : BaseViewModel() {

    private val getAddressUseCase: GetAddressUseCase by inject()
    private val getSendFeeUseCase: GetSendFeeUseCase by inject()
    private val screenApi: SendConnectConfirmScreenApi by inject()
    private val sendUseCase: SendUseCase by inject()
    private val snackBarController: SnackBarController by inject()
    private val tonConnectSendUseCase: TonConnectSendUseCase by inject()

    private var ufAddress = ""
    private var passCodeEntered = false
    private var sendJob: Job? = null

    val state = MutableStateFlow(SendConnectConfirmState(args.transfer.amount, "", null, false, false))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            ufAddress = getAddressUseCase.getUfAddress(args.transfer.rawAddress) ?: args.transfer.rawAddress
            state.value = state.value.copy(receiver = ufAddress)

            val fee = getSendFeeUseCase.invoke(ufAddress, args.transfer.amount, "")
            val feeString = Formatter.getFormattedAmount(fee)
            state.value = state.value.copy(feeString = "≈ $feeString TON")
        }
    }

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        if (!isStarted && !isPush && isEnter && passCodeEntered) {
            performSend()
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
        }
    }

    override fun onDestroy() {
        sendJob?.cancel()
        super.onDestroy()
    }

    fun onCancelClicked() {
        CoroutineScopes.appScope.launch(Dispatchers.IO) {
            val error = TonConnect.SendTransactionResponse.Error(
                id = args.transfer.requestId.toString(),
                error = TonConnect.Error(
                    code = 300,
                    message = "User declined the transaction"
                )
            )
            tonConnectSendUseCase.sendTransactionError(args.transfer.clientId, error)
        }
        screenApi.navigateBack()
    }

    fun onConfirmClicked() {
        if (sendJob?.isActive == true || state.value.feeString == null) {
            return
        }
        passCodeEntered = false
        screenApi.navigateToPassCodeEnter(PassCodeEnterPurposeConfirmSend)
    }

    private fun performSend() {
        if (sendJob?.isActive == true) {
            return
        }
        state.value = state.value.copy(isSending = true)
        sendJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                sendUseCase.invoke(ufAddress, args.transfer.amount, null)
                state.value = state.value.copy(isSent = true)
                CoroutineScopes.appScope.launch(Dispatchers.IO) {
                    val success = TonConnect.SendTransactionResponse.Success(
                        id = args.transfer.requestId.toString(),
                    )
                    tonConnectSendUseCase.sendTransactionResult(args.transfer.clientId, success)
                }
                withContext(Dispatchers.Main) {
                    delay(1000L)
                    screenApi.navigateBack()
                }
            } catch (e: Exception) {
                snackBarController.showMessage(SnackBarMessage(
                    title = null,
                    message = getErrorMessage(e),
                    drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                ))
            } finally {
                state.value = state.value.copy(isSending = false)
            }
        }
    }

    private companion object {

        private const val PassCodeEnterPurposeConfirmSend = "confirmSend"
    }
}