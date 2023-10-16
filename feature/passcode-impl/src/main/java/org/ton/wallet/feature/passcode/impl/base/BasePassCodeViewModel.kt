package org.ton.wallet.feature.passcode.impl.base

import android.os.Bundle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.ton.wallet.core.Res
import org.ton.wallet.data.auth.api.PassCodeType
import org.ton.wallet.feature.passcode.api.BasePasscodeScreenApi
import org.ton.wallet.feature.passcode.api.PassCodeSetupScreenApi
import org.ton.wallet.screen.viewmodel.BaseViewModel
import org.ton.wallet.strings.RString

abstract class BasePassCodeViewModel(
    screenArguments: BasePassCodeScreenArguments
) : BaseViewModel() {

    protected val isLoadingFlow = MutableStateFlow(false)
    protected val passCodeFlow = MutableStateFlow("")
    protected val passCodeTypeFlow = MutableStateFlow(PassCodeType.Pin4)

    protected val _errorEventFlow = Channel<Unit>(Channel.BUFFERED)
    val errorEventFlow: Flow<Unit> = _errorEventFlow.receiveAsFlow()

    protected val passCodeTotalLength: Int
        get() = passCodeTypeFlow.value.rawValue

    abstract val screenApi: BasePasscodeScreenApi
    val isBackVisible: Boolean = screenArguments.isBackVisible
    val isDark: Boolean = screenArguments.isDark

    val screenStateFlow = combine(
        passCodeFlow,
        passCodeTypeFlow,
        isLoadingFlow
    ) { passcode, type, isLoading ->
        PassCodeScreenState(
            title = title,
            subtitle = Res.str(RString.enter_passcode_digits, type.rawValue),
            optionsText = optionsText,
            passCodeLength = type.rawValue,
            filledDotsCount = passcode.length,
            isLoading = isLoading
        )
    }

    abstract val title: String?

    abstract val optionsText: String?

    abstract fun onNumberEntered(number: String)

    override fun onResultReceived(code: String, args: Bundle?) {
        super.onResultReceived(code, args)
        if (code == PassCodeSetupScreenApi.ResultCodePassCodeSet) {
            setResult(code, args)
        }
    }

    fun onBackClicked() {
        screenApi.navigateBack()
    }

    fun onBackSpaceClicked() {
        if (!isLoadingFlow.value) {
            passCodeFlow.value = passCodeFlow.value.dropLast(1)
        }
    }

    fun onClearClicked() {
        if (!isLoadingFlow.value) {
            passCodeFlow.value = ""
        }
    }

    fun onForDigitPassCodeClicked() {
        passCodeTypeFlow.value = PassCodeType.Pin4
        passCodeFlow.value = ""
    }

    fun onSixDigitPassCodeClicked() {
        passCodeTypeFlow.value = PassCodeType.Pin6
        passCodeFlow.value = ""
    }
}