package dev.sergey.triad.domain

sealed class Exercise {
    abstract val concept: Concept
    abstract val targetLang: AppLanguage
    abstract val nativeLang: AppLanguage

    data class ChoiceBank(
        val lang: AppLanguage,
        val options: List<String>,
        val correct: String,
    )

    data class Cloze(
        override val concept: Concept,
        override val nativeLang: AppLanguage,
        override val targetLang: AppLanguage,
        val banks: List<ChoiceBank>,
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
    val reviews: List<ReviewItem>,
    val exercise: Exercise,
    val showLearnFirst: Boolean = false,
) {
    val review: ReviewItem get() = reviews.first()
}

data class SessionPlan(
    val items: List<SessionItem>,
    val practice: Boolean = false,
)
