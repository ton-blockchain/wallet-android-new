package org.ton.wallet.feature.settings.api

import android.app.Activity
import android.content.Context

interface SettingsScreenApi {

    fun navigateToRecovery()

    fun navigateChangePassCode()

    fun navigateBack()

    fun navigateToSettings()

    fun navigateToPassCodeEnter(purpose: String, withBiometrics: Boolean)

    fun navigateToStart()

    fun showAppNotificationsSettings(context: Context, channelId: String)

    fun showBiometricEnrollAlert(context: Context)

    fun showBiometricPrompt(activity: Activity, onSuccess: () -> Unit)

    fun onPermissionRequested()

    fun shareLogs(activity: Activity)
}