package dev.sergey.triad.domain

import kotlin.random.Random

object PracticePlanner {
    const val STEP = 20

    fun sizeChoices(available: Int, step: Int = STEP): List<Int> {
        if (available <= 0 || step <= 0) return emptyList()
        val sizes = (step..available step step).toMutableList()
        if (sizes.isEmpty() || sizes.last() != available) {
            sizes += available
        }
        return sizes
    }

    fun eligible(
        profileId: Long,
        targetLangs: List<AppLanguage>,
        reviews: List<ReviewItem>,
        concepts: List<Concept>,
        unlockedThemeIds: Set<String>,
        primerThemeIds: Set<String>,
        masteredConceptIds: Set<String>,
    ): List<ReviewItem> {
        val conceptMap = concepts.associateBy { it.id }
        val targets = targetLangs.ifEmpty { AppLanguage.all }
        return reviews
            .filter { review ->
                review.profileId == profileId &&
                    review.reps > 0 &&
                    review.targetLang in targets &&
                    review.conceptId !in masteredConceptIds &&
                    conceptMap[review.conceptId]?.let { concept ->
                        concept.themeId in unlockedThemeIds && concept.themeId !in primerThemeIds
                    } == true
            }
            .groupBy { it.conceptId }
            .values
            .map { group ->
                targets.firstNotNullOfOrNull { lang -> group.firstOrNull { it.targetLang == lang } }
                    ?: group.first()
            }
    }

    fun pick(pool: List<ReviewItem>, limit: Int, random: Random): List<ReviewItem> {
        val n = limit.coerceIn(0, pool.size)
        return pool.shuffled(random).take(n)
    }
}
