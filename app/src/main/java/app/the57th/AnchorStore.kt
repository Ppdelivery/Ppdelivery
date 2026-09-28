package app.the57th

import android.content.Context
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

    const val MIN_YEAR = 1
    const val MAX_YEAR = 9999

    fun get(context: Context): YearMonth {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_YEAR)) {
            // First launch: anchor to this month, so the joke starts quietly and grows.
            return YearMonth.now().also { set(context, it) }
        }
        return YearMonth.of(prefs.getInt(KEY_YEAR, 0), prefs.getInt(KEY_MONTH, 1))
    }

    fun set(context: Context, anchor: YearMonth) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_YEAR, anchor.year)
            .putInt(KEY_MONTH, anchor.monthValue)
            .apply()
    }

    fun style(context: Context): FiftySeventh.Style =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_STYLE, null)
            ?.let { runCatching { FiftySeventh.Style.valueOf(it) }.getOrNull() }
            ?: FiftySeventh.Style.PHONE

    fun setStyle(context: Context, style: FiftySeventh.Style) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_STYLE, style.name).apply()
    }

    fun timely(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_TIMELY, false)

    fun setTimely(context: Context, timely: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_TIMELY, timely).apply()
    }

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
