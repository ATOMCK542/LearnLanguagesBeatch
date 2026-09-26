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
        preferredThemeId: String? = null,
        excludeConceptIds: Set<String> = emptySet(),
    ): SessionPlan {
        val targetLangs = profile.studyTargets()
        val pool = concepts.filter { it.id !in excludeConceptIds }
        val focused = preferredThemeId?.let { id -> pool.filter { it.themeId == id } }.orEmpty()
        val primary = focused.ifEmpty { pool }
        val planned = planFrom(profile, primary, reviews, now, sessionSize, targetLangs)
        if (planned.items.isNotEmpty() || focused.isEmpty()) return planned
        return planFrom(profile, pool, reviews, now, sessionSize, targetLangs)
    }

    private fun planFrom(
        profile: Profile,
        concepts: List<Concept>,
        reviews: List<ReviewItem>,
        now: Long,
        sessionSize: Int,
        targetLangs: List<AppLanguage>,
    ): SessionPlan {
        val conceptIds = concepts.map { it.id }.toSet()
        val byKey = reviews.associateBy { Triple(it.profileId, it.conceptId, it.targetLang) }
        val due = reviews
            .filter {
                it.profileId == profile.id &&
                    it.dueAt <= now &&
                    it.targetLang in targetLangs &&
                    it.conceptId in conceptIds
            }
            .sortedBy { it.dueAt }
        val existingKeys = due.map { it.conceptId to it.targetLang }.toSet()
        val newItems = mutableListOf<ReviewItem>()
        val remainingNew = profile.newLimit.coerceAtLeast(0)
        loop@ for (concept in concepts) {
            for (target in targetLangs) {
                if (newItems.size >= remainingNew) break@loop
                if (concept.id to target in existingKeys) continue
                if (byKey[Triple(profile.id, concept.id, target)] != null) continue
                newItems += scheduler.newItem(profile.id, concept.id, target, now)
            }
        }
        val selected = due + newItems
        val conceptOrder = selected.map { it.conceptId }.distinct().take(sessionSize)
        val grouped = selected.groupBy { it.conceptId }
        val conceptMap = concepts.associateBy { it.id }
        val items = conceptOrder.mapIndexedNotNull { index, conceptId ->
            val concept = conceptMap[conceptId] ?: return@mapIndexedNotNull null
            val cardReviews = grouped[conceptId].orEmpty()
            if (cardReviews.isEmpty()) return@mapIndexedNotNull null
            SessionItem(
                reviews = cardReviews,
                exercise = factory.forReview(
                    concept,
                    cardReviews.first(),
                    profile.nativeLang,
                    now,
                    index,
                    targetLangs,
                ),
                showLearnFirst = cardReviews.all { it.state == FsrsCardState.New && it.reps == 0 },
            )
        }
        return SessionPlan(items)
    }
}
