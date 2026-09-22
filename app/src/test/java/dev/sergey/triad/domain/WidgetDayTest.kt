package dev.sergey.triad.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDayTest {
    @Test
    fun sameDayIsClosed() {
        assertTrue(WidgetDay.isDone("2026-09-22", "2026-09-22"))
    }

    @Test
    fun nextDayOpensAgain() {
        assertFalse(WidgetDay.isDone("2026-09-22", "2026-09-23"))
    }

    @Test
    fun missingDayIsOpen() {
        assertFalse(WidgetDay.isDone(null, "2026-09-22"))
        assertFalse(WidgetDay.isDone("", "2026-09-22"))
    }
}
