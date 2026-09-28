package app.the57th.core

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * The whole joke: a month whose day number never rolls over.
 *
 * Plain proleptic-Gregorian date subtraction (java.time). No calendar reform,
 * no Julian conversion — "February 1776" is just the modern month label.
 */
object FiftySeventh {

    private const val MINUS = '−'

    /** Whole days from the 1st of [anchor] to [today], plus one. Negative for future anchors. */
    fun count(anchor: YearMonth, today: LocalDate): Long =
        ChronoUnit.DAYS.between(anchor.atDay(1), today) + 1

    /** "August the 57th". The year is deliberately dropped. */
    fun format(anchor: YearMonth, today: LocalDate): String =
        "${monthName(anchor)} the ${ordinal(count(anchor, today))}"

    fun monthName(anchor: YearMonth): String =
        anchor.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    /** 91549 -> "91,549th", -40 -> "−40th", 91311 -> "91,311th". */
    fun ordinal(n: Long): String = groupThousands(n) + suffix(n)

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

    /** Always a comma, regardless of device locale. */
    fun groupThousands(n: Long): String {
        val digits = Math.abs(n).toString()
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (n < 0) "$MINUS$grouped" else grouped
    }
}
