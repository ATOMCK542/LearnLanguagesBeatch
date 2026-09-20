package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PracticePlannerTest {
    private fun concept(id: String, theme: String = "cafe") = Concept(
        id = id,
        kind = ConceptKind.Word,
        themeId = theme,
        tags = emptyList(),
        grammar = LocalizedText("", "", ""),
        texts = AppLanguage.all.associateWith { lang ->
            ConceptText(lang, id, "", emptyMap(), emptyList())
        },
    )

    private fun review(
        profileId: Long,
        conceptId: String,
        lang: AppLanguage = AppLanguage.En,
        reps: Int = 2,
    ) = ReviewItem(
        profileId, conceptId, lang, 1.0, 5.0, 0, 1, reps, 0, FsrsCardState.Review, 1.0, 1.0,
    )

    @Test
    fun sizeChoicesStepTwentyAndAll() {
        assertEquals(emptyList<Int>(), PracticePlanner.sizeChoices(0))
        assertEquals(listOf(15), PracticePlanner.sizeChoices(15))
        assertEquals(listOf(20), PracticePlanner.sizeChoices(20))
        assertEquals(listOf(20, 40, 47), PracticePlanner.sizeChoices(47))
        assertEquals(listOf(20, 40), PracticePlanner.sizeChoices(40))
    }

    @Test
    fun eligibleDropsNewMasteredPrimersAndOtherProfiles() {
        val concepts = listOf(concept("a"), concept("b"), concept("p", "primer_en"), concept("c"))
        val reviews = listOf(
            review(1, "a"),
            review(1, "b", reps = 0),
            review(1, "p"),
            review(1, "c"),
            review(2, "a"),
        )
        val pool = PracticePlanner.eligible(
            profileId = 1,
            targetLangs = listOf(AppLanguage.En),
            reviews = reviews,
            concepts = concepts,
            unlockedThemeIds = setOf("cafe", "primer_en"),
            primerThemeIds = setOf("primer_en"),
            masteredConceptIds = setOf("c"),
        )
        assertEquals(listOf("a"), pool.map { it.conceptId })
        assertTrue(pool.all { it.profileId == 1L })
        assertFalse(pool.any { it.conceptId == "b" })
    }

    @Test
    fun pickShufflesAndCaps() {
        val pool = (1..8).map { review(1, "c$it") }
        val picked = PracticePlanner.pick(pool, 5, Random(1))
        assertEquals(5, picked.size)
        assertTrue(pool.map { it.conceptId }.containsAll(picked.map { it.conceptId }))
        val orders = (1L..24L).map { seed ->
            PracticePlanner.pick(pool, 5, Random(seed)).map { it.conceptId }
        }.toSet()
        assertTrue(orders.size > 1)
    }
}
