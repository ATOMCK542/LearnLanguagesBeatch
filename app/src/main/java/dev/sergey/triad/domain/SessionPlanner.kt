package dev.sergey.triad.domain

class SessionPlanner(
    private val scheduler: Scheduler,
    private val factory: ExerciseFactory,
) {
    fun plan(
        profile: Profile,
        concepts: List<Concept>,
        reviews: List<ReviewItem>,
        now: Long,
        sessionSize: Int = 10,
    ): SessionPlan {
        val targetLangs = profile.targetLangs.ifEmpty {
            AppLanguage.all.filter { it != profile.nativeLang }
        }
        val byKey = reviews.associateBy { Triple(it.profileId, it.conceptId, it.targetLang) }
        val due = reviews
            .filter { it.profileId == profile.id && it.dueAt <= now && it.targetLang in targetLangs }
            .sortedBy { it.dueAt }
        val existingKeys = due.map { it.conceptId to it.targetLang }.toSet()
        val newItems = mutableListOf<ReviewItem>()
        val remainingNew = (profile.newLimit).coerceAtLeast(0)
        loop@ for (concept in concepts) {
            for (target in targetLangs) {
                if (newItems.size >= remainingNew) break@loop
                if (concept.id to target in existingKeys) continue
                val existing = byKey[Triple(profile.id, concept.id, target)]
                if (existing != null) continue
                newItems += scheduler.newItem(profile.id, concept.id, target, now)
            }
        }
        val selected = (due + newItems).take(sessionSize)
        val conceptMap = concepts.associateBy { it.id }
        val items = selected.mapIndexedNotNull { index, review ->
            val concept = conceptMap[review.conceptId] ?: return@mapIndexedNotNull null
            SessionItem(
                review = review,
                exercise = factory.forReview(concept, review, profile.nativeLang, now, index),
            )
        }
        return SessionPlan(items)
    }
}
