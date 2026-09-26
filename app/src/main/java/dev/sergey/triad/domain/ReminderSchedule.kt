package dev.sergey.triad.domain

import java.time.LocalTime
import java.time.ZonedDateTime

object ReminderSchedule {
    const val HOUR = 19
    const val MINUTE = 0
    val time: LocalTime = LocalTime.of(HOUR, MINUTE)

    fun decide(
        now: ZonedDateTime,
        lastOpenDay: String?,
        lastNotifiedDay: String?,
        fromAlarm: Boolean,
    ): ReminderDecision {
        val today = now.toLocalDate().toString()
        val opened = lastOpenDay == today
        val alreadyNotified = lastNotifiedDay == today
        val afterSlot = !now.isBefore(slotOn(now))
        val notify = !opened && !alreadyNotified && (fromAlarm || afterSlot)
        return ReminderDecision(notify = notify, next = nextTrigger(now, fromAlarm))
    }

    fun nextTrigger(now: ZonedDateTime, fromAlarm: Boolean = false): ZonedDateTime {
        var slot = slotOn(now)
        if (!now.isBefore(slot)) slot = slot.plusDays(1)
        if (fromAlarm && !slot.isAfter(now.plusHours(12))) slot = slot.plusDays(1)
        return slot
    }

    private fun slotOn(now: ZonedDateTime): ZonedDateTime =
        now.withHour(HOUR).withMinute(MINUTE).withSecond(0).withNano(0)
}

data class ReminderDecision(
    val notify: Boolean,
    val next: ZonedDateTime,
)
