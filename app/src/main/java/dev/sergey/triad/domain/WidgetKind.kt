package dev.sergey.triad.domain

enum class WidgetKind {
    Lesson,
    Review,
    ;

    companion object {
        const val SESSION_SIZE = 5

        fun fromExtra(value: String?): WidgetKind? =
            entries.firstOrNull { it.name == value }
    }
}

object WidgetDay {
    fun isDone(storedDay: String?, today: String): Boolean =
        !storedDay.isNullOrBlank() && storedDay == today
}
