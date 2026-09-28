package app.the57th.core

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class FiftySeventhTest {

    private val today = LocalDate.of(2026, 9, 26)

    // --- The two worked examples from the brief. Both exact. ---

    @Test fun august2026() {
        val anchor = YearMonth.of(2026, 8)
        assertEquals(57L, FiftySeventh.count(anchor, today))
        assertEquals("August the 57th", FiftySeventh.format(anchor, today))
    }

    @Test fun february1776() {
        val anchor = YearMonth.of(1776, 2)
        assertEquals(91_549L, FiftySeventh.count(anchor, today))
        assertEquals("February the 91,549th", FiftySeventh.format(anchor, today))
    }

    // --- Day math ---

    @Test fun firstOfAnchorIsFirst() {
        assertEquals("September the 1st", FiftySeventh.format(YearMonth.of(2026, 9), LocalDate.of(2026, 9, 1)))
    }

    @Test fun currentMonthIsJustTheRealDate() {
        assertEquals("September the 26th", FiftySeventh.format(YearMonth.of(2026, 9), today))
    }

    @Test fun leapDayIsCounted() {
        // Feb 2024 has 29 days, so March 1 is the 30th.
        assertEquals(30L, FiftySeventh.count(YearMonth.of(2024, 2), LocalDate.of(2024, 3, 1)))
        assertEquals(29L, FiftySeventh.count(YearMonth.of(2025, 2), LocalDate.of(2025, 3, 1)))
    }

    @Test fun futureAnchorsGoNegative() {
        assertEquals(0L, FiftySeventh.count(YearMonth.of(2026, 10), LocalDate.of(2026, 9, 30)))
        assertEquals(-40L, FiftySeventh.count(YearMonth.of(2027, 2), LocalDate.of(2026, 12, 22)))
        assertEquals("February the −40th", FiftySeventh.format(YearMonth.of(2027, 2), LocalDate.of(2026, 12, 22)))
    }

    // --- Suffixes, with the teens exception applied per hundred ---

    @Test fun suffixes() {
        val expected = mapOf(
            1L to "1st", 2L to "2nd", 3L to "3rd", 4L to "4th", 10L to "10th",
            11L to "11th", 12L to "12th", 13L to "13th", 14L to "14th",
            21L to "21st", 22L to "22nd", 23L to "23rd",
            101L to "101st", 111L to "111th", 112L to "112th", 113L to "113th",
            1_001L to "1,001st",
            91_311L to "91,311th", 91_312L to "91,312th", 91_313L to "91,313th",
            91_321L to "91,321st", 91_322L to "91,322nd", 91_323L to "91,323rd",
            0L to "0th",
            -1L to "−1st", -11L to "−11th", -40L to "−40th",
        )
        expected.forEach { (n, s) -> assertEquals("n=$n", s, FiftySeventh.ordinal(n)) }
    }

    @Test fun thousandsSeparator() {
        assertEquals("999", FiftySeventh.groupThousands(999))
        assertEquals("1,000", FiftySeventh.groupThousands(1_000))
        assertEquals("91,549", FiftySeventh.groupThousands(91_549))
        assertEquals("1,234,567", FiftySeventh.groupThousands(1_234_567))
        assertEquals("−1,000", FiftySeventh.groupThousands(-1_000))
    }
}
