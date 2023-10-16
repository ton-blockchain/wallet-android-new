package org.ton.wallet.coreui.ext

import android.content.*
import android.os.*
import org.ton.wallet.lib.log.L

fun Context.copyToClipboard(content: String, message: String?, withVibrate: Boolean = true) {
    try {
        val clipboardManager = getSystemService(ClipboardManager::class.java)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("clipboard", content))
    } catch (e: Exception) {
        L.e(e)
    }
    if (withVibrate) {
        vibrate()
    }
}

fun Context.vibrate(durationMs: Long = 200) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        getSystemService(Vibrator::class.java).vibrate(effect)
    } else {
        getSystemService(Vibrator::class.java).vibrate(durationMs)
    }
}