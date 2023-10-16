package org.ton.wallet.uicomponents.snackbar

import android.graphics.drawable.Drawable
import androidx.annotation.AnyThread

interface SnackBarController {

    @AnyThread
    fun showMessage(message: SnackBarMessage)

    @AnyThread
    fun hideMessage()
}

class SnackBarMessage(
    val title: String?,
    val message: String? = null,
    val drawable: Drawable? = null,
    val durationMs: Long = 3000
)