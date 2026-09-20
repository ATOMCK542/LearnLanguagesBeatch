package dev.sergey.triad.domain

sealed class Exercise {
    abstract val concept: Concept
    abstract val targetLang: AppLanguage
    abstract val nativeLang: AppLanguage

    data class Reveal(
        override val concept: Concept,
        override val nativeLang: AppLanguage,
        override val targetLang: AppLanguage,
    ) : Exercise()

    data class Cloze(
        override val concept: Concept,
        override val nativeLang: AppLanguage,
        override val targetLang: AppLanguage,
        val options: List<String>,
        val correct: String,
    ) : Exercise()

    data class OrderChips(
        override val concept: Concept,
        override val nativeLang: AppLanguage,
        override val targetLang: AppLanguage,
        val shuffled: List<String>,
        val correct: List<String>,
    ) : Exercise()

    data class TonePick(
        override val concept: Concept,
        override val nativeLang: AppLanguage,
        override val targetLang: AppLanguage,
        val options: List<String>,
        val correct: String,
    ) : Exercise()
}

data class SessionItem(
    val review: ReviewItem,
    val exercise: Exercise,
)

data class SessionPlan(
    val items: List<SessionItem>,
)
