package dev.sergey.triad.domain

data class ThemeStudy(
    val covered: Int,
    val total: Int,
) {
    val studied: Boolean get() = total > 0 && covered >= total
}

object LessonProgress {
    fun byTheme(
        profileId: Long,
        concepts: List<Concept>,
        reviews: List<ReviewItem>,
        skipThemeIds: Set<String> = emptySet(),
    ): Map<String, ThemeStudy> {
        val covered = reviews
            .filter { it.profileId == profileId && it.reps > 0 }
            .map { it.conceptId }
            .toSet()
        return concepts
            .filter { it.themeId !in skipThemeIds && it.themeId != "user" }
            .groupBy { it.themeId }
            .mapValues { (_, group) ->
                val ids = group.map { it.id }.toSet()
                ThemeStudy(covered = ids.count { it in covered }, total = ids.size)
            }
    }

    /** Cards from a finished lesson that were never saved with an answer. */
    fun stillUnseen(planned: List<ReviewItem>, existing: List<ReviewItem>): List<ReviewItem> {
        val seen = existing
            .filter { it.reps > 0 }
            .map { Triple(it.profileId, it.conceptId, it.targetLang) }
            .toSet()
        return planned
            .distinctBy { Triple(it.profileId, it.conceptId, it.targetLang) }
            .filter { Triple(it.profileId, it.conceptId, it.targetLang) !in seen }
    }
}
