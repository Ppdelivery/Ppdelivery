package app.the57th.core

import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FiftySeventhTest {

    private val today = LocalDate.of(2026, 9, 26)
    private val us = Locale.US

    private fun us(anchor: YearMonth, on: LocalDate = today, timely: Boolean = false) =
        FiftySeventh.format(anchor, on, us, "MMMM d", timely = timely)

    // --- The two worked examples from the brief. Both exact. ---

    @Test fun august2026() {
        val anchor = YearMonth.of(2026, 8)
        assertEquals(57L, FiftySeventh.count(anchor, today))
        assertEquals("August the 57th", us(anchor))
    }

    @Test fun february1776() {
        val anchor = YearMonth.of(1776, 2)
        assertEquals(91_549L, FiftySeventh.count(anchor, today))
        assertEquals("February the 91,549th", us(anchor))
    }

    // --- Day math ---

    @Test fun firstOfAnchorIsFirst() {
        assertEquals("September the 1st", us(YearMonth.of(2026, 9), LocalDate.of(2026, 9, 1)))
    }

    @Test fun leapDayIsCounted() {
        assertEquals(30L, FiftySeventh.count(YearMonth.of(2024, 2), LocalDate.of(2024, 3, 1)))
        assertEquals(29L, FiftySeventh.count(YearMonth.of(2025, 2), LocalDate.of(2025, 3, 1)))
    }

    @Test fun futureAnchorsGoNegative() {
        assertEquals(0L, FiftySeventh.count(YearMonth.of(2026, 10), LocalDate.of(2026, 9, 30)))
        assertEquals("February the -40th", us(YearMonth.of(2027, 2), LocalDate.of(2026, 12, 22)))
    }

    // --- English suffixes, teens exception per hundred ---

    @Test fun suffixes() {
        val expected = mapOf(
            1L to "1st", 2L to "2nd", 3L to "3rd", 4L to "4th", 11L to "11th", 12L to "12th",
            13L to "13th", 21L to "21st", 22L to "22nd", 23L to "23rd", 101L to "101st",
            111L to "111th", 1_001L to "1,001st", 91_311L to "91,311th", 91_312L to "91,312th",
            91_313L to "91,313th", 91_321L to "91,321st", 0L to "0th", -1L to "-1st", -11L to "-11th",
        )
        expected.forEach { (n, s) -> assertEquals("n=$n", s, FiftySeventh.ordinal(n, us)) }
    }

    // --- Matching the phone ---

    @Test fun britishEnglishPutsTheDayFirst() {
        assertEquals("the 57th of August",
            FiftySeventh.format(YearMonth.of(2026, 8), today, Locale.UK, "d MMMM"))
    }

    @Test fun germanUsesItsOwnPatternAndSeparator() {
        assertEquals("91.549. Februar",
            FiftySeventh.format(YearMonth.of(1776, 2), today, Locale.GERMANY, "d. MMMM"))
    }

    @Test fun frenchUsesItsOwnSeparator() {
        val s = FiftySeventh.format(YearMonth.of(1776, 2), today, Locale.FRANCE, "d MMMM")
        assertEquals("91 549 février", s)
    }

    @Test fun russianUsesTheGenitiveMonth() {
        assertEquals("57 августа",
            FiftySeventh.format(YearMonth.of(2026, 8), today, Locale.forLanguageTag("ru"), "d MMMM"))
    }

    @Test fun quotedLiteralsInPatternsSurvive() {
        assertEquals("57 'de' agosto".replace("'", ""),
            FiftySeventh.format(YearMonth.of(2026, 8), today, Locale.forLanguageTag("es"), "d 'de' MMMM"))
    }

    // --- Timely wording ---

    @Test fun timelyEras() {
        assertEquals("the .lvij. day of August", us(YearMonth.of(1350, 8), LocalDate.of(1350, 9, 26), timely = true))
        assertEquals("the 57th Day of August", us(YearMonth.of(1650, 8), LocalDate.of(1650, 9, 26), timely = true))
        assertEquals("Feby. 91,549th", us(YearMonth.of(1776, 2), timely = true))
        assertEquals("the 57th inst.", us(YearMonth.of(1850, 8), LocalDate.of(1850, 9, 26), timely = true))
        assertEquals("AUGUST 57TH STOP", us(YearMonth.of(1920, 8), LocalDate.of(1920, 9, 26), timely = true))
        assertEquals("AUG 57", us(YearMonth.of(1985, 8), LocalDate.of(1985, 9, 26), timely = true))
        assertEquals("August the 57th", us(YearMonth.of(2026, 8), timely = true))
    }

    @Test fun timelyFallsBackForFutureAnchorsAndOtherLanguages() {
        assertEquals("February the -40th", us(YearMonth.of(2027, 2), LocalDate.of(2026, 12, 22), timely = true))
        assertEquals("91.549. Februar",
            FiftySeventh.format(YearMonth.of(1776, 2), today, Locale.GERMANY, "d. MMMM", timely = true))
    }

    // --- Classic style: fixed English wording whatever the phone says ---

    private fun classic(anchor: YearMonth, on: LocalDate = today, locale: Locale = Locale.GERMANY, timely: Boolean = false) =
        FiftySeventh.format(anchor, on, locale, "d. MMMM", FiftySeventh.Style.CLASSIC, timely)

    @Test fun classicIgnoresThePhone() {
        assertEquals("August the 57th", classic(YearMonth.of(2026, 8)))
        assertEquals("February the 91,549th", classic(YearMonth.of(1776, 2)))
        assertEquals("February the 91,549th", classic(YearMonth.of(1776, 2), locale = Locale.UK))
        assertEquals("February the \u221240th", classic(YearMonth.of(2027, 2), LocalDate.of(2026, 12, 22)))
    }

    @Test fun classicCanBeTimelyOnAnyPhone() {
        assertEquals("Feby. 91,549th", classic(YearMonth.of(1776, 2), timely = true))
    }

    @Test fun medievalNumerals() {
        assertEquals("i", TimelyWording.roman(1))
        assertEquals("iij", TimelyWording.roman(3))
        assertEquals("xiv", TimelyWording.roman(14))
        assertEquals("M", TimelyWording.roman(1000))
        assertEquals("xcj.M.dxlix", TimelyWording.roman(91_549))
    }
}
