package dev.sergey.triad.domain

enum class AppLanguage(val code: String) {
    En("en"),
    Ru("ru"),
    Vi("vi");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: error("Unsupported language: $code")

        val all: List<AppLanguage> = entries.toList()
    }
}
