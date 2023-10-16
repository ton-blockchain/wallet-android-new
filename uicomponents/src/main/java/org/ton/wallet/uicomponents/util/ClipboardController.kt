package org.ton.wallet.uicomponents.util

import android.content.Context
import android.os.Build
import org.ton.wallet.core.Res
import org.ton.wallet.coreui.ext.copyToClipboard
import org.ton.wallet.uicomponents.snackbar.SnackBarController
import org.ton.wallet.uicomponents.snackbar.SnackBarMessage
import org.ton.wallet.uikit.RUiKitColor
import org.ton.wallet.uikit.RUiKitDrawable

interface ClipboardController {

    fun copyToClipboard(content: String, message: String?, withVibrate: Boolean = true)
}

class ClipboardControllerImpl(
    private val context: Context,
    private val snackBarController: SnackBarController
) : ClipboardController {

    override fun copyToClipboard(content: String, message: String?, withVibrate: Boolean) {
        context.copyToClipboard(content, message, withVibrate)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            message?.let { msg ->
                val snackBarMessage = SnackBarMessage(
                    title = null,
                    message = msg,
                    drawable = Res.drawableColored(RUiKitDrawable.ic_copy_28, Res.color(RUiKitColor.common_white))
                )
                snackBarController.showMessage(snackBarMessage)
            }
        }
    }
}