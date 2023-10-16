package org.ton.wallet.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import org.ton.wallet.lib.log.L

object AppIntentUtils {

    var isAppIntentStarted = false

    fun openAppSettings(activity: Activity) {
        isAppIntentStarted = true
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

    fun openAppNotificationsSettings(context: Context, channelId: String?) {
        isAppIntentStarted = true
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channelId == null) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                }
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:" + context.packageName)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun shareText(context: Context, text: String, chooserTitle: String = "", ) {
        isAppIntentStarted = true
        val sendIntent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        startChooserIntent(context, sendIntent, chooserTitle)
    }

    private fun startChooserIntent(context: Context, intent: Intent, title: String) {
        try {
            val chooserIntent = Intent.createChooser(intent, title)
            if (intent.extras?.containsKey(Intent.EXTRA_STREAM) == true) {
                val resolvedActivities = context.packageManager.queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                for (resolveInfo in resolvedActivities) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            L.e(e)
        }
    }
}