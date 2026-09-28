package app.the57th.core

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * The whole joke: a month whose day number never rolls over.
 *
 * Plain proleptic-Gregorian date subtraction (java.time). No calendar reform,
 * no Julian conversion — "February 1776" is just the modern month label.
 */
object FiftySeventh {

    /** Stands in for the day while the platform's month-day pattern is rendered. */
    private const val DAY_SLOT = '\uE000'
    private const val MINUS = '\u2212'

    /** Whole days from the 1st of [anchor] to [today], plus one. Negative for future anchors. */
    fun count(anchor: YearMonth, today: LocalDate): Long =
        ChronoUnit.DAYS.between(anchor.atDay(1), today) + 1

    /** How the date is worded. */
    enum class Style {
        /** Follow the phone: its language, word order, digits, separators and minus sign. */
        PHONE,
        /** The original: English month + ordinal, comma thousands, a real minus sign (−). */
        CLASSIC,
    }

    /**
     * Today's never-ending date. The year is deliberately dropped.
     *
     * @param monthDayPattern the platform's best pattern for the "MMMMd" skeleton in
     *   [locale] — e.g. "MMMM d" (en-US), "d MMMM" (en-GB, fr, ru), "d. MMMM" (de).
     *   Ignored for [Style.CLASSIC].
     * @param timely word it the way it would have been written in the anchor's era
     *   (English only; anything else falls back to the modern wording).
     */
    fun format(
        anchor: YearMonth,
        today: LocalDate,
        locale: Locale,
        monthDayPattern: String,
        style: Style = Style.PHONE,
        timely: Boolean = false,
    ): String {
        val n = count(anchor, today)
        if (style == Style.CLASSIC) {
            val text = if (timely) TimelyWording.english(anchor, n, Locale.US) else null
            return (text ?: english(anchor, n, Locale.US, dayFirst = false)).replace('-', MINUS)
        }
        if (locale.language == "en") {
            if (timely) TimelyWording.english(anchor, n, locale)?.let { return it }
            return english(anchor, n, locale, dayFirst = dayBeforeMonth(monthDayPattern))
        }
        return fromPattern(anchor, number(n, locale), locale, monthDayPattern)
    }

    /** "August the 57th" (month-first locales) or "the 57th of August" (day-first). */
    private fun english(anchor: YearMonth, n: Long, locale: Locale, dayFirst: Boolean): String {
        val month = monthName(anchor, locale)
        val nth = ordinal(n, locale)
        return if (dayFirst) "the $nth of $month" else "$month the $nth"
    }

    /** Render the locale's own month-day pattern with our count in the day's place. */
    private fun fromPattern(anchor: YearMonth, day: String, locale: Locale, pattern: String): String {
        val rendered = runCatching {
            DateTimeFormatter.ofPattern(slotDay(pattern), locale).format(anchor.atDay(1))
        }.getOrNull()
        if (rendered == null || DAY_SLOT !in rendered) {
            return "${monthName(anchor, locale)} $day"
        }
        return rendered.replace(DAY_SLOT.toString(), day)
    }

    /** Swap each unquoted run of 'd' for a quoted placeholder literal. */
    internal fun slotDay(pattern: String): String {
        val out = StringBuilder()
        var quoted = false
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            when {
                c == '\'' -> { quoted = !quoted; out.append(c); i++ }
                !quoted && c == 'd' -> {
                    while (i < pattern.length && pattern[i] == 'd') i++
                    out.append('\'').append(DAY_SLOT).append('\'')
                }
                else -> { out.append(c); i++ }
            }
        }
        return out.toString()
    }

    private fun dayBeforeMonth(pattern: String): Boolean {
        val unquoted = pattern.replace(Regex("'[^']*'"), "")
        val d = unquoted.indexOf('d')
        val m = unquoted.indexOfFirst { it == 'M' || it == 'L' }
        return d >= 0 && m >= 0 && d < m
    }

    fun monthName(anchor: YearMonth, locale: Locale): String =
        DateTimeFormatter.ofPattern("MMMM", locale).format(anchor.atDay(1))

    /** The count with the locale's digits, grouping and minus sign: 91,549 / 91.549 / 91 549. */
    fun number(n: Long, locale: Locale): String = NumberFormat.getIntegerInstance(locale).format(n)

    /** English ordinal: "91,549th", "91,311th", "-40th". */
    fun ordinal(n: Long, locale: Locale = Locale.US): String = number(n, locale) + suffix(n)

    fun suffix(n: Long): String {
        val abs = Math.abs(n)
        if (abs % 100 in 11..13) return "th"
        return when (abs % 10) {
            1L -> "st"
            2L -> "nd"
            3L -> "rd"
            else -> "th"
        }
    }
}
