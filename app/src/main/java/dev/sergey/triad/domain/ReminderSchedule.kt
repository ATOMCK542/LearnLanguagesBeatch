package dev.sergey.triad.domain

import java.time.LocalTime
import java.time.ZonedDateTime

object ReminderSchedule {
    val defaultTime: LocalTime = LocalTime.of(19, 0)

    fun decide(
        now: ZonedDateTime,
        lastOpenDay: String?,
        lastNotifiedDay: String?,
        fromAlarm: Boolean,
        time: LocalTime = defaultTime,
    ): ReminderDecision {
        val today = now.toLocalDate().toString()
        val opened = lastOpenDay == today
        val alreadyNotified = lastNotifiedDay == today
        val afterSlot = !now.isBefore(slotOn(now, time))
        val notify = !opened && !alreadyNotified && (fromAlarm || afterSlot)
        return ReminderDecision(notify = notify, next = nextTrigger(now, fromAlarm, time))
    }

    fun nextTrigger(
        now: ZonedDateTime,
        fromAlarm: Boolean = false,
        time: LocalTime = defaultTime,
    ): ZonedDateTime {
        var slot = slotOn(now, time)
        if (!now.isBefore(slot)) slot = slot.plusDays(1)
        if (fromAlarm && !slot.isAfter(now.plusHours(12))) slot = slot.plusDays(1)
        return slot
    }

    private fun slotOn(now: ZonedDateTime, time: LocalTime): ZonedDateTime =
        now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
}

data class ReminderDecision(
    val notify: Boolean,
    val next: ZonedDateTime,
)
