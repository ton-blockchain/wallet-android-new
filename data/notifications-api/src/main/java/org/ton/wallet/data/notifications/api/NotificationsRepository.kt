package org.ton.wallet.data.notifications.api

import android.app.Activity
import android.content.Context
import androidx.core.app.NotificationCompat
import org.ton.wallet.data.core.BaseRepository
import pub.devrel.easypermissions.PermissionRequest

interface NotificationsRepository : BaseRepository {

    val idTonConnectAction: Int

    val defaultChannelId: String

    fun getPermissionRequest(activity: Activity, requestCode: Int): PermissionRequest?

    fun showNotification(context: Context, id: Int, builder: NotificationCompat.Builder)
}