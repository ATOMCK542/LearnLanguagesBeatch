package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseFactoryTest {
    private val factory = ExerciseFactory { listOf("one", "two", "three", "four", "five") }
    private val concept = Concept(
        id = "s1",
        kind = ConceptKind.Sentence,
        themeId = "q",
        tags = listOf("question"),
        grammar = LocalizedText("g", "г", "n"),
        texts = mapOf(
            AppLanguage.En to ConceptText(AppLanguage.En, "I want coffee", "", mapOf(AppLanguage.Ru to "ай"), emptyList()),
            AppLanguage.Ru to ConceptText(AppLanguage.Ru, "Я хочу кофе", "", emptyMap(), emptyList()),
            AppLanguage.Vi to ConceptText(AppLanguage.Vi, "Tôi muốn cà phê", "", emptyMap(), listOf("ngang", "hỏi", "huyền", "ngang")),
        ),
    )

    @Test
    fun clozeIncludesCorrectOption() {
        val cloze = factory.cloze(concept, AppLanguage.Ru, AppLanguage.En, 42)
        val bank = cloze.banks.single()
        assertEquals(AppLanguage.En, bank.lang)
        assertEquals(4, bank.options.size)
        assertTrue(bank.options.contains(bank.correct))
    }

    @Test
    fun clozeHasABankPerStudiedLanguage() {
        val cloze = factory.cloze(
            concept,
            AppLanguage.Ru,
            AppLanguage.En,
            7,
            listOf(AppLanguage.En, AppLanguage.Vi),
        )
        assertEquals(listOf(AppLanguage.En, AppLanguage.Vi), cloze.banks.map { it.lang })
        assertTrue(cloze.banks[0].options.contains("I want coffee"))
        assertTrue(cloze.banks[1].options.contains("Tôi muốn cà phê"))
    }

    @Test
    fun orderIsNotIdentityIfPossible() {
        val order = factory.order(concept, AppLanguage.Ru, AppLanguage.En, 1)
        assertEquals(listOf("I", "want", "coffee"), order.correct)
        if (order.correct.size > 1) {
            assertNotEquals(order.correct, order.shuffled)
        }
    }

    @Test
    fun clozeShufflesCorrectAmongOptions() {
        val positions = (1L..48L).map { seed ->
            factory.cloze(concept, AppLanguage.Ru, AppLanguage.En, seed).banks.single().options.indexOf("I want coffee")
        }.toSet()
        assertTrue(positions.size > 1)
        assertTrue(positions.any { it > 0 })
    }

    @Test
    fun toneShufflesCorrectAmongOptions() {
        val positions = (1L..48L).map { seed ->
            factory.tone(concept, AppLanguage.En, AppLanguage.Vi, seed).options.indexOf(
                "ngang · hỏi · huyền · ngang",
            )
        }.toSet()
        assertTrue(positions.size > 1)
        assertTrue(positions.any { it > 0 })
    }

    @Test
    fun shuffleChoicesMovesIdentityOrder() {
        val original = listOf("a", "b", "c", "d")
        val moved = ExerciseFactory.shuffleChoices(original, 0)
        assertTrue(moved.containsAll(original))
        assertEquals(4, moved.size)
        assertNotEquals(original, moved)
    }

    @Test
    fun forReviewPicksRevealForNewAndGamesForKnown() {
        val brandNew = ReviewItem(1, "s1", AppLanguage.Vi, 1.0, 5.0, 0, null, 0, 0, FsrsCardState.New, 0.0, 0.0)
        val known = brandNew.copy(state = FsrsCardState.Review, reps = 3)
        val reveal = factory.forReview(concept, brandNew.copy(targetLang = AppLanguage.En), AppLanguage.Ru, 1L, 0)
        assertTrue(reveal is Exercise.Cloze)
        val stillReveal = factory.forReview(concept, brandNew.copy(targetLang = AppLanguage.En), AppLanguage.Ru, 1L, 1)
        assertTrue(stillReveal is Exercise.Cloze)
        val cloze = factory.forReview(concept, known.copy(targetLang = AppLanguage.En), AppLanguage.Ru, 1L, 0)
        assertTrue(cloze is Exercise.Cloze)
        val order = factory.forReview(concept, known.copy(targetLang = AppLanguage.En), AppLanguage.Ru, 1L, 2)
        assertTrue(order is Exercise.OrderChips)
        val tone = factory.forReview(concept, known, AppLanguage.En, 1L, 4)
        assertTrue(tone is Exercise.TonePick)
        val tokenized = ExerciseFactory.tokenize("  I   want  ")
        assertEquals(listOf("I", "want"), tokenized)
    }
}
