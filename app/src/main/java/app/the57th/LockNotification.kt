package app.the57th

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * A quiet, ongoing notification carrying the count, so it shows on the lock screen
 * (where apps can't otherwise draw). Default importance so Android doesn't file it
 * under "silent" and hide it from the lock screen, but with no sound or vibration.
 */
object LockNotification {
    private const val CHANNEL = "today"
    private const val ID = 57

    /** Posts or clears the notification per the setting; returns whether it's showing. */
    fun update(context: Context): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (!AnchorStore.lockNotification(context)) {
            manager.cancel(ID)
            return false
        }
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_today)
            .setContentTitle(AnchorStore.todayText(context))
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
        manager.notify(ID, notification)
        return true
    }
}
