package app.the57th

import android.content.Context
import java.time.YearMonth

/** The one piece of state: which month never ends. Shared by the app and every widget. */
object AnchorStore {
    private const val PREFS = "anchor"
    private const val KEY_YEAR = "year"
    private const val KEY_MONTH = "month"

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
}
