package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewIntervalTest {
    @Test
    fun minutesBelowOneHour() {
        assertEquals(IntervalParts(1, IntervalUnit.Minute), ReviewInterval.ofDelay(0))
        assertEquals(IntervalParts(10, IntervalUnit.Minute), ReviewInterval.ofDelay(10 * 60_000L))
        assertEquals(IntervalParts(59, IntervalUnit.Minute), ReviewInterval.ofDelay(59 * 60_000L))
    }

    @Test
    fun hoursThenDays() {
        assertEquals(IntervalParts(1, IntervalUnit.Hour), ReviewInterval.ofDelay(60 * 60_000L))
        assertEquals(IntervalParts(5, IntervalUnit.Hour), ReviewInterval.ofDelay(5 * 60 * 60_000L))
        assertEquals(IntervalParts(1, IntervalUnit.Day), ReviewInterval.ofDelay(24 * 60 * 60_000L))
        assertEquals(IntervalParts(4, IntervalUnit.Day), ReviewInterval.ofDelay(4 * 24 * 60 * 60_000L))
    }
}
