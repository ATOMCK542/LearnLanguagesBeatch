package dev.sergey.triad.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderScheduleTest {
    private val zone = ZoneId.of("Asia/Bangkok")

    @Test
    fun morningBootWaitsForEvening() {
        val decision = ReminderSchedule.decide(at("2026-09-26", "10:00"), null, null, fromAlarm = false)
        assertFalse(decision.notify)
        assertEquals(at("2026-09-26", "19:00"), decision.next)
    }

    @Test
    fun eveningAlarmNotifiesAndSchedulesTomorrow() {
        val decision = ReminderSchedule.decide(at("2026-09-26", "19:00"), null, null, fromAlarm = true)
        assertTrue(decision.notify)
        assertEquals(at("2026-09-27", "19:00"), decision.next)
    }

    @Test
    fun openedTodaySkipsTheReminder() {
        val decision = ReminderSchedule.decide(at("2026-09-26", "19:00"), "2026-09-26", null, fromAlarm = true)
        assertFalse(decision.notify)
        assertEquals(at("2026-09-27", "19:00"), decision.next)
    }

    @Test
    fun lateBootStillRemindsOnce() {
        val decision = ReminderSchedule.decide(at("2026-09-26", "21:15"), null, null, fromAlarm = false)
        assertTrue(decision.notify)
        assertEquals(at("2026-09-27", "19:00"), decision.next)
    }

    @Test
    fun secondAlarmTheSameDayStaysQuiet() {
        val decision = ReminderSchedule.decide(at("2026-09-26", "19:00"), null, "2026-09-26", fromAlarm = true)
        assertFalse(decision.notify)
    }

    @Test
    fun earlyAlarmDoesNotRescheduleItself() {
        val next = ReminderSchedule.nextTrigger(at("2026-09-26", "18:59"), fromAlarm = true)
        assertEquals(at("2026-09-27", "19:00"), next)
    }

    @Test
    fun customTimeIsTheSlot() {
        val time = LocalTime.of(8, 30)
        val waiting = ReminderSchedule.decide(at("2026-09-26", "07:00"), null, null, fromAlarm = false, time = time)
        assertFalse(waiting.notify)
        assertEquals(at("2026-09-26", "08:30"), waiting.next)
        val missed = ReminderSchedule.decide(at("2026-09-26", "09:00"), null, null, fromAlarm = false, time = time)
        assertTrue(missed.notify)
        assertEquals(at("2026-09-27", "08:30"), missed.next)
    }

    private fun at(day: String, time: String): ZonedDateTime =
        ZonedDateTime.of(LocalDate.parse(day), LocalTime.parse(time), zone)
}
