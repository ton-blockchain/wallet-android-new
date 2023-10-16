package org.ton.wallet.app.screen

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import org.ton.wallet.app.activity.MainActivity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppIntentUtils
import org.ton.wallet.feature.scanqr.api.ScanQrScreenApi

internal class ScanQrScreenApiImpl(
    private val navigator: Navigator
) : ScanQrScreenApi {

    override fun navigateBack() {
        navigator.pop(false)
    }

    override fun navigateToAppSettings(activity: Activity) {
        AppIntentUtils.openAppSettings(activity)
    }

    override fun navigateToImagePicker(activity: Activity) {
        (activity as MainActivity).showImagePicker(ActivityResultContracts.PickVisualMedia.ImageOnly)
    }
}