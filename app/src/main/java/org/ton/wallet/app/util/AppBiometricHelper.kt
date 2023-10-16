package org.ton.wallet.app.util

import android.content.*
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.ton.wallet.core.Res
import org.ton.wallet.lib.security.BiometricUtils
import org.ton.wallet.strings.RString
import org.ton.wallet.uicomponents.dialog.AlertDialog
import javax.crypto.Cipher

object AppBiometricHelper {

    private const val BiometricKeyAlias = "a"

    fun showBiometricPrompt(activity: FragmentActivity, description: String, callback: BiometricPrompt.AuthenticationCallback, ) {
        if (BiometricUtils.isBiometricsNoneEnrolled(activity)) {
            showBiometricEnrollAlert(activity)
        } else {
            val cipher = BiometricUtils.getBiometricCipher(AppKeystoreUtils.keyStore, AppKeystoreUtils.AndroidKeyStoreName, BiometricKeyAlias)
            showBiometricPrompt(activity, description, cipher, BiometricUtils.Authenticators, callback)
        }
    }

    fun showBiometricPrompt(activity: FragmentActivity, description: String, cipher: Cipher?, authenticators: Int, callback: BiometricPrompt.AuthenticationCallback) {
        if (authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL == 0 && BiometricUtils.isBiometricsNoneEnrolled(activity)) {
            showBiometricEnrollAlert(activity)
        } else {
            showBiometricPromptInternal(activity, description, cipher, authenticators, callback)
        }
    }

    fun showBiometricEnrollAlert(context: Context): AlertDialog {
        val alertDialog = AlertDialog.Builder(
            title = Res.str(RString.enable_biometrics),
            message = Res.str(RString.biometric_enroll_message),
            positiveButton = Res.str(RString.open_settings) to DialogInterface.OnClickListener { dialog, _ ->
                showBiometricEnroll(context)
                dialog.dismiss()
            },
            negativeButton = Res.str(RString.cancel) to DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }
        ).build(context)
        alertDialog.show()
        return alertDialog
    }

    private fun showBiometricPromptInternal(
        activity: FragmentActivity,
        description: String,
        cipher: Cipher?,
        authenticators: Int,
        callback: BiometricPrompt.AuthenticationCallback,
    ) {
        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
        var promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(authenticators)
            .setTitle(Res.str(RString.biometric_prompt_title))
            .setSubtitle(Res.str(RString.app_name))
            .setDescription(description)
        if (authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL == 0) {
            promptInfoBuilder = promptInfoBuilder.setNegativeButtonText(Res.str(RString.cancel))
        }
        val promptInfo = promptInfoBuilder.build()
        try {
            if (cipher == null) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        } catch (e: KeyPermanentlyInvalidatedException) {
            clearBiometricKey()
        }
    }

    private fun showBiometricEnroll(context: Context) {
        val intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
            intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricUtils.Authenticators)
        } else {
            intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun clearBiometricKey() {
        AppKeystoreUtils.keyStore.deleteEntry(BiometricKeyAlias)
    }
}