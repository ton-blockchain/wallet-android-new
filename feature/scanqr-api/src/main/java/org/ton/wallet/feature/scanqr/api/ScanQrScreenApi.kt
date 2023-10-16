package org.ton.wallet.feature.scanqr.api

import android.app.Activity

interface ScanQrScreenApi {

    fun navigateBack()

    fun navigateToAppSettings(activity: Activity)

    fun navigateToImagePicker(activity: Activity)

    companion object {

        const val ResultCodeQrDetected = "qrDetected"
        const val ArgumentKeyQrValue = "value"
        const val ArgumentKeyLinkAction = "linkAction"
    }
}