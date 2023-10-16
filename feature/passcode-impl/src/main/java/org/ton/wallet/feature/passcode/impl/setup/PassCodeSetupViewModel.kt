package org.ton.wallet.feature.passcode.impl.setup

import kotlinx.coroutines.*
import org.ton.wallet.core.Res
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.data.auth.api.PassCodeType
import org.ton.wallet.feature.passcode.api.PassCodeSetupScreenApi
import org.ton.wallet.feature.passcode.impl.base.BasePassCodeViewModel
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitDrawable

class PassCodeSetupViewModel(
    private val args: PassCodeSetupScreenArguments
) : BasePassCodeViewModel(args) {

    private val authRepository: AuthRepository by inject()
    private val snackBarController: SnackBarController by inject()

    private val isConfirmPasscode: Boolean
        get() = !args.passCode.isNullOrEmpty()

    override val screenApi: PassCodeSetupScreenApi by inject()

    override val title: String =
        if (isConfirmPasscode) Res.str(RString.confirm_passcode)
        else Res.str(RString.set_a_passcode)

    override val optionsText: String? =
        if (isConfirmPasscode) null
        else Res.str(RString.passcode_options)

    init {
        passCodeTypeFlow.value =
            if (args.passCode?.length == 6) PassCodeType.Pin6
            else PassCodeType.Pin4
    }

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        if (!isStarted && isPush && !isEnter) {
            passCodeFlow.value = ""
        }
    }

    override fun onNumberEntered(number: String) {
        val passCodeTotalLength = passCodeTypeFlow.value.rawValue
        if (passCodeFlow.value.length + 1 > passCodeTotalLength) {
            return
        }

        passCodeFlow.value = passCodeFlow.value + number
        snackBarController.hideMessage()

        if (passCodeFlow.value.length == passCodeTotalLength) {
            val passCodeToCheck = passCodeFlow.value
            viewModelScope.launch(Dispatchers.Default) {
                delay(200)
                if (isConfirmPasscode) {
                    if (passCodeToCheck == args.passCode) {
                        isLoadingFlow.tryEmit(true)
                        val type = if (args.passCode.length == 4) PassCodeType.Pin4 else PassCodeType.Pin6
                        authRepository.setPassCode(passCodeToCheck, type)
                        isLoadingFlow.tryEmit(false)
                        setResult(PassCodeSetupScreenApi.ResultCodePassCodeSet)
                    } else {
                        val message = SnackBarMessage(
                            title = Res.str(RString.error),
                            message = Res.str(RString.passcode_not_matches),
                            drawable = Res.drawable(RUiKitDrawable.ic_warning_32)
                        )
                        snackBarController.showMessage(message)
                        _errorEventFlow.trySend(Unit)
                        passCodeFlow.value = ""
                    }
                } else {
                    screenApi.navigateToPassCodeSetupCheck(passCodeToCheck, args.withBiometrics)
                }
            }
        }
    }
}