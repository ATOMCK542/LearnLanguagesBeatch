package dev.sergey.triad.domain

import dev.sergey.triad.data.scheduler.FsrsScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPlannerTest {
    private val scheduler = FsrsScheduler()
    private val factory = ExerciseFactory { lang -> listOf("alpha", "beta", "gamma", "delta") }

    private fun concept(id: String) = Concept(
        id = id,
        kind = ConceptKind.Sentence,
        themeId = "cafe",
        tags = listOf("theme:cafe"),
        grammar = LocalizedText("g", "г", "ng"),
        texts = AppLanguage.all.associateWith { lang ->
            ConceptText(lang, "I want coffee", "/aɪ/", mapOf(AppLanguage.Ru to "ай"), emptyList())
        },
    )

    @Test
    fun doesNotMixProfiles() {
        val now = 10L
        val profile = Profile(1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru, listOf(AppLanguage.En), 5, true, 0, null, 0)
        val other = scheduler.newItem(99, "x", AppLanguage.En, 0)
        val dueMine = scheduler.newItem(1, "c1", AppLanguage.En, 0).let { scheduler.review(it, Rating.Good, 0) }.copy(dueAt = 0)
        val plan = SessionPlanner(scheduler, factory).plan(profile, listOf(concept("c1"), concept("c2")), listOf(other, dueMine), now)
        assertTrue(plan.items.all { it.review.profileId == 1L })
        assertTrue(plan.items.any { it.review.conceptId == "c1" })
    }

    @Test
    fun usesNativeToTarget() {
        val now = 5L
        val profile = Profile(1, "A", null, now, now, AppLanguage.Vi, AppLanguage.Vi, listOf(AppLanguage.En), 3, true, 0, null, 0)
        val plan = SessionPlanner(scheduler, factory).plan(profile, listOf(concept("c1")), emptyList(), now)
        assertEquals(AppLanguage.Vi, plan.items.first().exercise.nativeLang)
        assertEquals(AppLanguage.En, plan.items.first().exercise.targetLang)
    }

    @Test
    fun groupsStudiedLanguagesOnOneCard() {
        val now = 8L
        val profile = Profile(
            1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru,
            listOf(AppLanguage.En, AppLanguage.Vi), 4, true, 0, null, 0,
        )
        val en = scheduler.newItem(1, "c1", AppLanguage.En, 0).copy(dueAt = 0)
        val vi = scheduler.newItem(1, "c1", AppLanguage.Vi, 0).copy(dueAt = 0)
        val plan = SessionPlanner(scheduler, factory).plan(
            profile,
            listOf(concept("c1")),
            listOf(en, vi),
            now,
        )
        assertEquals(1, plan.items.size)
        assertEquals(2, plan.items[0].reviews.size)
        val cloze = plan.items[0].exercise as Exercise.Cloze
        assertEquals(setOf(AppLanguage.En, AppLanguage.Vi), cloze.banks.map { it.lang }.toSet())
    }

    @Test
    fun preferredThemeIsUsedBeforeOthers() {
        val now = 3L
        val profile = Profile(1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru, listOf(AppLanguage.En), 5, true, 0, null, 0)
        val cafe = concept("cafe.1").copy(themeId = "cafe")
        val home = concept("home.1").copy(id = "home.1", themeId = "home")
        val plan = SessionPlanner(scheduler, factory).plan(
            profile,
            listOf(cafe, home),
            emptyList(),
            now,
            preferredThemeId = "home",
        )
        assertEquals(listOf("home.1"), plan.items.map { it.review.conceptId })
    }

    @Test
    fun prefersFallbackWhenPreferredThemeAlreadyScheduled() {
        val now = 4L
        val profile = Profile(1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru, listOf(AppLanguage.En), 5, true, 0, null, 0)
        val cafe = concept("cafe.1").copy(themeId = "cafe")
        val home = concept("home.1").copy(id = "home.1", themeId = "home")
        val later = scheduler.newItem(1, "home.1", AppLanguage.En, now).let {
            scheduler.review(it, Rating.Good, now)
        }
        val plan = SessionPlanner(scheduler, factory).plan(
            profile,
            listOf(cafe, home),
            listOf(later),
            now,
            preferredThemeId = "home",
        )
        assertTrue(plan.items.any { it.review.conceptId == "cafe.1" })
    }

    @Test
    fun nativeOnlyTargetsStillStudyOtherLanguages() {
        val now = 6L
        val profile = Profile(1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru, listOf(AppLanguage.Ru), 3, true, 0, null, 0)
        val plan = SessionPlanner(scheduler, factory).plan(profile, listOf(concept("c1")), emptyList(), now)
        assertTrue(plan.items.isNotEmpty())
        assertTrue(plan.items.first().reviews.none { it.targetLang == AppLanguage.Ru })
    }
}
