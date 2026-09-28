package app.the57th.core

import java.time.YearMonth
import java.util.Locale

/**
 * "Timely wording": the date phrased the way it would have been written in the
 * anchor's own era. English only; the eras are picked for flavour, not scholarship.
 */
object TimelyWording {

    private val MIDDLE_ENGLISH_MONTHS = listOf(
        "Januarie", "Feverer", "March", "Aprill", "May", "Juyn",
        "Juyl", "August", "Septembre", "Octobre", "Novembre", "Decembre",
    )

    // Georgian letter-heading abbreviations.
    private val GEORGIAN_MONTHS = listOf(
        "Jany.", "Feby.", "March", "April", "May", "June",
        "July", "Augt.", "Septr.", "Octr.", "Novr.", "Decr.",
    )

    /** Null when there's no period wording for this anchor (future anchors, day zero or less). */
    fun english(anchor: YearMonth, n: Long, locale: Locale): String? {
        if (n < 1) return null
        val m = anchor.monthValue - 1
        val month = FiftySeventh.monthName(anchor, locale)
        val nth = FiftySeventh.ordinal(n, locale)
        return when (anchor.year) {
            in Int.MIN_VALUE..1499 -> "the .${roman(n)}. day of ${MIDDLE_ENGLISH_MONTHS[m]}"
            in 1500..1699 -> "the $nth Day of $month"
            in 1700..1799 -> "${GEORGIAN_MONTHS[m]} $nth"
            in 1800..1899 -> "the $nth inst."
            in 1900..1959 -> "$month $nth STOP".uppercase(locale)
            in 1960..1999 -> "${month.take(3).uppercase(locale)} ${FiftySeventh.number(n, locale)}"
            else -> null
        }
    }

    /**
     * Medieval-style lower-case numerals: a final i written as j (vij, iij), and
     * thousands written as a multiplier before M (.xcj.M.dxlix. = 91,549).
     */
    fun roman(n: Long): String {
        require(n >= 1)
        val thousands = n / 1000
        val rest = n % 1000
        val parts = buildList {
            if (thousands == 1L) add("M")
            else if (thousands > 1) add(roman(thousands) + ".M")
            if (rest > 0) add(small(rest.toInt()))
        }
        return parts.joinToString(".")
    }

    private fun small(n: Int): String {
        val numerals = listOf(
            900 to "cm", 500 to "d", 400 to "cd", 100 to "c", 90 to "xc", 50 to "l",
            40 to "xl", 10 to "x", 9 to "ix", 5 to "v", 4 to "iv", 1 to "i",
        )
        var left = n
        val s = StringBuilder()
        for ((value, glyph) in numerals) while (left >= value) { s.append(glyph); left -= value }
        if (s.length > 1 && s.endsWith("i")) s.setCharAt(s.length - 1, 'j')
        return s.toString()
    }
}
