package org.ton.wallet.feature.scanqr.impl

import android.app.Activity
import android.content.Context
import android.os.Bundle
import org.ton.wallet.coreui.ext.vibrate
import org.ton.wallet.data.core.link.LinkAction
import org.ton.wallet.data.core.link.LinkUtils
import org.ton.wallet.feature.scanqr.api.ScanQrScreenApi
import org.ton.wallet.lib.log.L
import org.ton.wallet.screen.viewmodel.BaseViewModel

class ScanQrViewModel(private val args: ScanQrScreenArguments) : BaseViewModel() {

    private val context: Context by inject()
    private val screenApi: ScanQrScreenApi by inject()

    fun onBackClicked() {
        screenApi.navigateBack()
    }

    fun onOpenSettingsClicked(activity: Activity) {
        screenApi.navigateToAppSettings(activity)
    }

    fun onImageClicked(activity: Activity) {
        screenApi.navigateToImagePicker(activity)
    }

    fun onQrDetected(value: String): Boolean {
        L.d("QR string: $value")
        val linkAction = LinkUtils.parseLink(value) ?: return false
        val linkActionType = getLinkActionType(linkAction)
        return if (args.linkActionTypes.contains(linkActionType)) {
            context.vibrate()
            val bundle = Bundle()
            bundle.putParcelable(ScanQrScreenApi.ArgumentKeyLinkAction, linkAction)
            bundle.putString(ScanQrScreenApi.ArgumentKeyQrValue, value)
            setResult(ScanQrScreenApi.ResultCodeQrDetected, bundle)
            true
        } else {
            false
        }
    }

    private fun getLinkActionType(linkAction: LinkAction): Int {
        return when (linkAction) {
            is LinkAction.TransferAction -> ScanQrScreenArguments.LinkActionTypeTransfer
            is LinkAction.TonConnectAction -> ScanQrScreenArguments.LinkActionTypeTonConnect
        }
    }
}