package org.ton.wallet.data.notifications.impl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.*
import androidx.core.content.ContextCompat
import org.ton.wallet.core.Res
import org.ton.wallet.data.notifications.api.NotificationsRepository
import org.ton.wallet.strings.RString
import pub.devrel.easypermissions.PermissionRequest

class NotificationsRepositoryImpl(
    channelIdPrefix: String
) : NotificationsRepository {

    override val idTonConnectAction: Int = 0

    override val defaultChannelId: String = channelIdPrefix

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    private val isNeedNotificationPermission: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private var isChannelCreated = false

    override fun getPermissionRequest(activity: Activity, requestCode: Int): PermissionRequest? {
        if (!isNeedNotificationPermission) {
            return null
        }
        return PermissionRequest.Builder(activity, requestCode, Manifest.permission.POST_NOTIFICATIONS)
            .setRationale(Res.str(RString.notifications_permission_description))
            .setPositiveButtonText(Res.str(RString.ok))
            .setNegativeButtonText(Res.str(RString.cancel))
            .build()
    }

    override fun showNotification(context: Context, id: Int, builder: NotificationCompat.Builder) {
        createChannelIfNeeded(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notification = builder
                .setVibrate(longArrayOf(1000, 1000))
                .build()
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    override suspend fun deleteWallet() = Unit

    private fun createChannelIfNeeded(context: Context) {
        if (isChannelCreated || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannelCompat.Builder(defaultChannelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(Res.str(RString.app_name))
            .setVibrationEnabled(true)
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}