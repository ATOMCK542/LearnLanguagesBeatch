package dev.sergey.triad.domain

data class LocalizedText(
    val en: String,
    val ru: String,
    val vi: String,
) {
    fun forLang(lang: AppLanguage): String = when (lang) {
        AppLanguage.En -> en
        AppLanguage.Ru -> ru
        AppLanguage.Vi -> vi
    }
}

enum class ConceptKind { Word, Phrase, Sentence }

data class ConceptText(
    val lang: AppLanguage,
    val text: String,
    val ipa: String,
    val hints: Map<AppLanguage, String>,
    val tones: List<String> = emptyList(),
)

data class Concept(
    val id: String,
    val kind: ConceptKind,
    val themeId: String,
    val tags: List<String>,
    val grammar: LocalizedText,
    val texts: Map<AppLanguage, ConceptText>,
) {
    fun text(lang: AppLanguage): ConceptText =
        texts[lang] ?: error("Missing ${lang.code} for $id")
}

data class Theme(
    val id: String,
    val kind: String,
    val sortOrder: Int,
    val title: LocalizedText,
    val description: LocalizedText = LocalizedText("", "", ""),
)

data class Profile(
    val id: Long,
    val displayName: String,
    val email: String?,
    val createdAt: Long,
    val lastOpenedAt: Long,
    val uiLang: AppLanguage,
    val nativeLang: AppLanguage,
    val targetLangs: List<AppLanguage>,
    val newLimit: Int,
    val ttsEnabled: Boolean,
    val streakDays: Int,
    val lastStudyDay: String?,
    val reviewsDone: Int,
)

enum class FsrsCardState { New, Learning, Review, Relearning }

enum class Rating { Again, Hard, Good, Easy }

data class ReviewItem(
    val profileId: Long,
    val conceptId: String,
    val targetLang: AppLanguage,
    val stability: Double,
    val difficulty: Double,
    val dueAt: Long,
    val lastReviewAt: Long?,
    val reps: Int,
    val lapses: Int,
    val state: FsrsCardState,
    val elapsedDays: Double,
    val scheduledDays: Double,
)

data class UserCard(
    val id: Long,
    val profileId: Long,
    val conceptId: String,
)

data class PrimerProgress(
    val profileId: Long,
    val primerId: String,
    val completed: Boolean,
)

data class PathProgress(
    val profileId: Long,
    val themeId: String,
    val unlocked: Boolean,
    val completedCount: Int,
)
