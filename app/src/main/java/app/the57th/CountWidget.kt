package app.the57th

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.the57th.core.FiftySeventh
import java.time.LocalDate
import java.time.ZoneId

/**
 * Home-screen widget: the real clock (a TextClock, ticked by the system) next to
 * the never-ending day count. Reads the system date; never writes it.
 */
class CountWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, views(context)) }
        scheduleMidnight(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MIDNIGHT,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> refreshAll(context)
            else -> super.onReceive(context, intent)
        }
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(midnightIntent(context))
    }

    companion object {
        private const val ACTION_MIDNIGHT = "app.the57th.action.MIDNIGHT"

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CountWidget::class.java))
            if (ids.isEmpty()) return
            ids.forEach { manager.updateAppWidget(it, views(context)) }
            scheduleMidnight(context)
        }

        private fun views(context: Context): RemoteViews {
            val count = FiftySeventh.format(AnchorStore.get(context), LocalDate.now())
            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            return RemoteViews(context.packageName, R.layout.widget).apply {
                setTextViewText(R.id.widget_count, count)
                setOnClickPendingIntent(R.id.widget_root, open)
            }
        }

        /**
         * Wake just after local midnight to bump the count. A windowed (inexact) alarm
         * needs no exact-alarm permission; a minute's slack is fine for a joke.
         */
        private fun scheduleMidnight(context: Context) {
            val zone = ZoneId.systemDefault()
            val nextMidnight = LocalDate.now(zone).plusDays(1).atStartOfDay(zone)
                .toInstant().toEpochMilli()
            context.getSystemService(AlarmManager::class.java).setWindow(
                AlarmManager.RTC, nextMidnight, 60_000L, midnightIntent(context),
            )
        }

        private fun midnightIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context, 0,
                Intent(context, CountWidget::class.java).setAction(ACTION_MIDNIGHT),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }
}
