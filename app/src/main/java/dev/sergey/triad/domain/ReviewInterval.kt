package dev.sergey.triad.domain

enum class IntervalUnit { Minute, Hour, Day }

data class IntervalParts(val count: Int, val unit: IntervalUnit)

object ReviewInterval {
    fun ofDelay(delayMs: Long): IntervalParts {
        val minutes = (delayMs / 60_000L).coerceAtLeast(1)
        return when {
            minutes < 60 -> IntervalParts(minutes.toInt(), IntervalUnit.Minute)
            minutes < 24 * 60 -> IntervalParts((minutes / 60).toInt().coerceAtLeast(1), IntervalUnit.Hour)
            else -> IntervalParts((minutes / (24 * 60)).toInt().coerceAtLeast(1), IntervalUnit.Day)
        }
    }
}
