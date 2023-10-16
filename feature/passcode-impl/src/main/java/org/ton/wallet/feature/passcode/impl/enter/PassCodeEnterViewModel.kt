package org.ton.wallet.feature.passcode.impl.enter

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import org.ton.wallet.core.Res
import org.ton.wallet.core.ext.weak
import org.ton.wallet.data.auth.api.AuthRepository
import org.ton.wallet.feature.passcode.api.PassCodeEnterScreenApi
import org.ton.wallet.feature.passcode.impl.base.BasePassCodeViewModel
import org.ton.wallet.strings.RString
import java.lang.ref.WeakReference

class PassCodeEnterViewModel(
    private val screenArguments: PassCodeEnterScreenArguments
) : BasePassCodeViewModel(screenArguments) {

    private val authRepository: AuthRepository by inject()
    private var isEnterPushChangeEnded = false
    private var activityRef = WeakReference<Activity?>(null)

    override val screenApi: PassCodeEnterScreenApi by inject()
    override val title: String? = null

    override val optionsText: String? =
        if (authRepository.isBiometricActiveFlow.value && !screenArguments.isOnlyPassCode) Res.str(RString.biometric_auth)
        else null

    init {
        screenArguments.passCodeType?.let { type -> passCodeTypeFlow.value = type }
    }

    override fun onScreenChange(isStarted: Boolean, isPush: Boolean, isEnter: Boolean) {
        super.onScreenChange(isStarted, isPush, isEnter)
        isEnterPushChangeEnded = !isStarted && isPush && isEnter
        checkAndShowBiometricPrompt()
    }

    override fun onActivityResumed() {
        super.onActivityResumed()
        checkAndShowBiometricPrompt()
    }

    override fun onNumberEntered(number: String) {
        if (passCodeFlow.value.length + 1 > passCodeTotalLength || isLoadingFlow.value) {
            return
        }

        passCodeFlow.value = passCodeFlow.value + number
        if (passCodeFlow.value.length == passCodeTotalLength) {
            val passCodeToCheck = passCodeFlow.value
            isLoadingFlow.tryEmit(true)
            if (screenArguments.isPassCodeToResult) {
                val bundle = Bundle()
                bundle.putString(PassCodeEnterScreenApi.ArgumentKeyPurpose, screenArguments.purpose)
                bundle.putString(PassCodeEnterScreenApi.ArgumentKeyPassCode, passCodeToCheck)
                setResult(PassCodeEnterScreenApi.ResultKeyPassCodeEntered, bundle)
            } else {
                viewModelScope.launch(Dispatchers.Default) {
                    delay(200L)
                    val isPassCodeCorrect = authRepository.checkPassCode(passCodeToCheck)
                    if (isPassCodeCorrect) {
                        onAuthSuccess()
                    } else {
                        _errorEventFlow.trySend(Unit)
                        isLoadingFlow.tryEmit(false)
                        passCodeFlow.tryEmit("")
                    }
                }
            }
        }
    }

    fun setActivity(activity: Activity) {
        activityRef = weak(activity)
    }

    fun showBiometricPrompt(activity: FragmentActivity) {
        if (authRepository.isBiometricActiveFlow.value) {
            screenApi.showBiometricPrompt(activity, Res.str(RString.biometric_prompt_default_description)) {
                val fakePassCode = StringBuilder()
                repeat(passCodeTotalLength) { fakePassCode.append("x") }
                passCodeFlow.tryEmit(fakePassCode.toString())
                onAuthSuccess()
            }
        }
    }

    private fun onAuthSuccess() {
        val bundle = Bundle()
        bundle.putString(PassCodeEnterScreenApi.ArgumentKeyPurpose, screenArguments.purpose)
        setResult(PassCodeEnterScreenApi.ResultKeyPassCodeEntered, bundle)
    }

    private fun checkAndShowBiometricPrompt() {
        val isNeedShowBiometricPrompt = isEnterPushChangeEnded && !screenArguments.isOnlyPassCode && authRepository.isBiometricActiveFlow.value
        if (isNeedShowBiometricPrompt) {
            val activity = activityRef.get() ?: return
            screenApi.showBiometricPrompt(activity, Res.str(RString.biometric_prompt_default_description)) {
                val fakePassCode = StringBuilder()
                repeat(passCodeTotalLength) { fakePassCode.append("x") }
                passCodeFlow.tryEmit(fakePassCode.toString())
                onAuthSuccess()
            }
        }
    }
}