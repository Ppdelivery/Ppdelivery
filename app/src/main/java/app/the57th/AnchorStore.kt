package app.the57th

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import app.the57th.core.FiftySeventh
import java.time.LocalDate
import java.time.YearMonth

/** Which month never ends, and how to word it. Shared by the app and every widget. */
object AnchorStore {
    private const val PREFS = "anchor"
    private const val KEY_YEAR = "year"
    private const val KEY_MONTH = "month"
    private const val KEY_STYLE = "style"
    private const val KEY_TIMELY = "timely"
    private const val KEY_LOCK_NOTIFICATION = "lock_notification"

    const val MIN_YEAR = 1
    const val MAX_YEAR = 9999

    fun get(context: Context): YearMonth {
        val stored = prefs(context)
        if (!stored.contains(KEY_YEAR)) {
            // First launch: anchor to this month, so the joke starts quietly and grows.
            return YearMonth.now().also { set(context, it) }
        }
        return YearMonth.of(stored.getInt(KEY_YEAR, 0), stored.getInt(KEY_MONTH, 1))
    }

    fun set(context: Context, anchor: YearMonth) {
        prefs(context).edit()
            .putInt(KEY_YEAR, anchor.year)
            .putInt(KEY_MONTH, anchor.monthValue)
            .apply()
    }

    fun style(context: Context): FiftySeventh.Style =
        prefs(context)
            .getString(KEY_STYLE, null)
            ?.let { runCatching { FiftySeventh.Style.valueOf(it) }.getOrNull() }
            ?: FiftySeventh.Style.PHONE

    fun setStyle(context: Context, style: FiftySeventh.Style) {
        prefs(context).edit()
            .putString(KEY_STYLE, style.name).apply()
    }

    fun timely(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TIMELY, false)

    fun setTimely(context: Context, timely: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_TIMELY, timely).apply()
    }

    fun lockNotification(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LOCK_NOTIFICATION, false)

    fun setLockNotification(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(KEY_LOCK_NOTIFICATION, on).apply()
    }

    /** For listeners (the wallpaper redraws when anything here changes). */
    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Today's never-ending date, worded per the phone's language and the user's settings. */
    fun todayText(context: Context): String {
        val locale = context.resources.configuration.locales[0]
        return FiftySeventh.format(
            anchor = get(context),
            today = LocalDate.now(),
            locale = locale,
            monthDayPattern = DateFormat.getBestDateTimePattern(locale, "MMMMd"),
            style = style(context),
            timely = timely(context),
        )
    }
}
