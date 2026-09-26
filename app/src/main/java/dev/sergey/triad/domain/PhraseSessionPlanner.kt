package dev.sergey.triad.domain

class PhraseSessionPlanner(
    private val scheduler: Scheduler,
    private val factory: ExerciseFactory,
) {
    fun plan(
        profile: Profile,
        concepts: List<Concept>,
        recipes: List<PhraseRecipe>,
        reviews: List<ReviewItem>,
        now: Long,
        sessionSize: Int = SESSION_SIZE,
    ): SessionPlan {
        val byKey = reviews.associateBy { Triple(it.profileId, it.conceptId, it.targetLang) }
        val slots = PhraseUnlock.ready(recipes, profile.id, profile.studyTargets(), reviews, now)
            .sortedWith(
                compareBy(
                    { it.recipe.level },
                    { byKey[Triple(profile.id, it.recipe.conceptId, it.targetLang)]?.dueAt ?: Long.MAX_VALUE },
                ),
            )
            .take(sessionSize)
        val conceptMap = concepts.associateBy { it.id }
        val items = slots.mapIndexedNotNull { index, slot ->
            val concept = conceptMap[slot.recipe.conceptId] ?: return@mapIndexedNotNull null
            val review = byKey[Triple(profile.id, concept.id, slot.targetLang)]
                ?: scheduler.newItem(profile.id, concept.id, slot.targetLang, now)
            SessionItem(
                reviews = listOf(review),
                exercise = factory.order(concept, profile.nativeLang, slot.targetLang, now + index),
                showLearnFirst = review.state == FsrsCardState.New && review.reps == 0,
            )
        }
        return SessionPlan(items)
    }

    companion object {
        const val SESSION_SIZE = 8
    }
}
