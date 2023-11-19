package org.ton.wallet.app.action

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.ton.wallet.app.activity.MainActivity
import org.ton.wallet.app.navigation.Navigator
import org.ton.wallet.app.util.AppLifecycleDetector
import org.ton.wallet.core.Res
import org.ton.wallet.data.notifications.api.NotificationsRepository
import org.ton.wallet.data.settings.api.SettingsRepository
import org.ton.wallet.feature.send.impl.connect.SendConnectConfirmScreenArguments
import org.ton.wallet.lib.tonconnect.*
import org.ton.wallet.screen.AppScreen
import org.ton.wallet.strings.RString
import org.ton.wallet.uikit.RUiKitDrawable

interface TonConnectEventHandler {

    suspend fun onTonConnectEvent(event: TonConnectEvent)
}

internal class TonConnectEventHandlerImpl(
    private val navigator: Navigator,
    private val notificationsRepository: NotificationsRepository,
    private val settingsRepository: SettingsRepository,
    private val tonConnectClient: TonConnectClient
) : TonConnectEventHandler {

    override suspend fun onTonConnectEvent(event: TonConnectEvent) {
        val isAppForeground = AppLifecycleDetector.isAppForegroundFlow.value
        if (event.request is TonConnectApi.SendTransactionRequest) {
            val isPasscodeAtTop = navigator.topScreenTag == AppScreen.PassCodeEnter.name
            if (isAppForeground && !isPasscodeAtTop) {
                tonConnectClient.setEventShowed(event.clientId, event.eventId)
                navigator.push(SendConnectConfirmScreenArguments(event))
            } else if (settingsRepository.isNotificationsOn.value) {
                val intent = Intent(Res.context, MainActivity::class.java)
                intent.putExtra(MainActivity.ArgumentKeyTonConnectAction, event)

                var flags = PendingIntent.FLAG_UPDATE_CURRENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    flags = flags or PendingIntent.FLAG_MUTABLE
                }
                val pendingIntent = PendingIntent.getActivity(Res.context, 0, intent, flags)

                val builder = NotificationCompat.Builder(Res.context, notificationsRepository.defaultChannelId)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(Res.str(RString.notification_ton_connect_title))
                    .setContentText(Res.str(RString.notification_ton_connect_description))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(RUiKitDrawable.ic_gem_18)
                notificationsRepository.showNotification(Res.context, notificationsRepository.idTonConnectAction, builder)
            }
        } else if (event.request is TonConnectApi.DisconnectRequest) {
            tonConnectClient.disconnect(event.clientId)
        }
    }
}