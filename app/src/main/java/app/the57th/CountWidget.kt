package app.the57th

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Home-screen widget: the real clock (a TextClock, ticked by the system) next to
 * the never-ending day count. Reads the system date; never writes it.
 */
class CountWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        Today.refreshEverywhere(context)

    // Last widget removed: stop the midnight alarm unless the notification still needs it.
    override fun onDisabled(context: Context) = Today.refreshEverywhere(context)

    companion object {
        /** Redraws every placed widget; returns whether there are any. */
        fun update(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CountWidget::class.java))
            ids.forEach { manager.updateAppWidget(it, views(context)) }
            return ids.isNotEmpty()
        }

        private fun views(context: Context): RemoteViews {
            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            return RemoteViews(context.packageName, R.layout.widget).apply {
                setTextViewText(R.id.widget_count, AnchorStore.todayText(context))
                setOnClickPendingIntent(R.id.widget_root, open)
            }
        }
    }
}
