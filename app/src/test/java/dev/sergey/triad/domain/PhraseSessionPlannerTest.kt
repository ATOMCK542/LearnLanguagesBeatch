package dev.sergey.triad.domain

import dev.sergey.triad.data.scheduler.FsrsScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseSessionPlannerTest {
    private val scheduler = FsrsScheduler()
    private val factory = ExerciseFactory { emptyList() }
    private val now = 5_000L

    private fun concept(id: String, text: String) = Concept(
        id = id,
        kind = ConceptKind.Sentence,
        themeId = "composed",
        tags = listOf("composed"),
        grammar = LocalizedText("", "", ""),
        texts = AppLanguage.all.associateWith { lang ->
            ConceptText(lang, text, "", emptyMap(), emptyList())
        },
    )

    private fun learned(words: Set<String>) = words.map {
        ReviewItem(1, it, AppLanguage.En, 1.0, 5.0, 0, 1, 1, 0, FsrsCardState.Review, 1.0, 1.0)
    }

    @Test
    fun ordersByLevelAndUsesWordOrderExercise() {
        val easy = PhraseRecipe("l1", 1, setOf("a", "b"))
        val hard = PhraseRecipe("l3", 3, setOf("a", "b", "c", "d"))
        val profile = Profile(
            1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru,
            listOf(AppLanguage.En), 5, true, 0, null, 0,
        )
        val plan = PhraseSessionPlanner(scheduler, factory).plan(
            profile,
            listOf(concept("l3", "I want hot coffee"), concept("l1", "I want")),
            listOf(hard, easy),
            learned(setOf("a", "b", "c", "d")),
            now,
        )
        assertEquals(listOf("l1", "l3"), plan.items.map { it.review.conceptId })
        assertTrue(plan.items.all { it.exercise is Exercise.OrderChips })
        val chips = plan.items.first().exercise as Exercise.OrderChips
        assertEquals(listOf("I", "want"), chips.correct)
    }

    @Test
    fun skipsPhraseWhenAWordIsMissing() {
        val recipe = PhraseRecipe("l1", 1, setOf("a", "b"))
        val profile = Profile(
            1, "A", null, now, now, AppLanguage.Ru, AppLanguage.Ru,
            listOf(AppLanguage.En), 5, true, 0, null, 0,
        )
        val plan = PhraseSessionPlanner(scheduler, factory).plan(
            profile,
            listOf(concept("l1", "I want")),
            listOf(recipe),
            learned(setOf("a")),
            now,
        )
        assertTrue(plan.items.isEmpty())
    }
}
