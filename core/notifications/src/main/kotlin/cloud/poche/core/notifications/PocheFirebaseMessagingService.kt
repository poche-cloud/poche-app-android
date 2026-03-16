package cloud.poche.core.notifications

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cloud.poche.core.datastore.UserPreferencesDataSource
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PocheFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userPreferencesDataSource: UserPreferencesDataSource

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notification = remoteMessage.notification ?: return
        val title = notification.title
        val body = notification.body
        if (title == null || body == null) return

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName) ?: Intent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            NotificationManagerCompat.from(this)
                .notify(remoteMessage.messageId.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userPreferencesDataSource.setFcmToken(token)
        }
    }
}
