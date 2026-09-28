package app.the57th

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

/**
 * Keeps every place that shows the count (widgets, lock-screen notification) current.
 * The live wallpaper keeps its own time while it's visible.
 */
object Today {
    private const val ACTION_MIDNIGHT = "app.the57th.action.MIDNIGHT"

    fun refreshEverywhere(context: Context) {
        val widgets = CountWidget.update(context)
        val notification = LockNotification.update(context)
        val alarms = context.getSystemService(AlarmManager::class.java)
        if (widgets || notification) {
            // A windowed (inexact) alarm needs no exact-alarm permission; a minute's slack is fine.
            alarms.setWindow(AlarmManager.RTC, nextMidnightMillis(), 60_000L, midnightIntent(context))
        } else {
            alarms.cancel(midnightIntent(context))
        }
    }

    fun nextMidnightMillis(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun midnightIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 0,
            Intent(context, DayReceiver::class.java).setAction(ACTION_MIDNIGHT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}

/** Midnight, clock or timezone changes, a new phone language, reboots and app updates. */
class DayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Today.refreshEverywhere(context)
}
