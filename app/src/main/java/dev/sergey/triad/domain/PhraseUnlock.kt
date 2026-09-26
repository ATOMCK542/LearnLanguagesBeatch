package dev.sergey.triad.domain

data class PhraseRecipe(
    val conceptId: String,
    val level: Int,
    val wordIds: Set<String>,
)

data class PhraseSlot(
    val recipe: PhraseRecipe,
    val targetLang: AppLanguage,
)

object PhraseUnlock {
    fun learnedIds(
        profileId: Long,
        targetLang: AppLanguage,
        reviews: List<ReviewItem>,
    ): Set<String> = reviews
        .filter { it.profileId == profileId && it.targetLang == targetLang && it.reps > 0 }
        .map { it.conceptId }
        .toSet()

    fun open(recipes: List<PhraseRecipe>, learned: Set<String>): List<PhraseRecipe> =
        recipes.filter { recipe ->
            recipe.wordIds.isNotEmpty() && recipe.wordIds.all { it in learned }
        }

    fun ready(
        recipes: List<PhraseRecipe>,
        profileId: Long,
        targets: List<AppLanguage>,
        reviews: List<ReviewItem>,
        now: Long,
    ): List<PhraseSlot> {
        val byKey = reviews.associateBy { Triple(it.profileId, it.conceptId, it.targetLang) }
        return targets.flatMap { target ->
            val learned = learnedIds(profileId, target, reviews)
            open(recipes, learned).mapNotNull { recipe ->
                val existing = byKey[Triple(profileId, recipe.conceptId, target)]
                val actionable = existing == null || existing.reps == 0 || existing.dueAt <= now
                if (actionable) PhraseSlot(recipe, target) else null
            }
        }
    }
}
