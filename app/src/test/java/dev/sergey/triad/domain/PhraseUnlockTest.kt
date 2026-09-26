package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseUnlockTest {
    private val now = 1_000L

    private fun review(
        profileId: Long,
        conceptId: String,
        lang: AppLanguage = AppLanguage.En,
        reps: Int = 1,
        dueAt: Long = 0,
    ) = ReviewItem(
        profileId, conceptId, lang, 1.0, 5.0, dueAt, 1, reps, 0,
        if (reps == 0) FsrsCardState.New else FsrsCardState.Review,
        1.0, 1.0,
    )

    @Test
    fun staysClosedUntilEveryWordHasAnAnswer() {
        val recipe = PhraseRecipe("cmp.l1.i_want", 1, setOf("pron.i", "verb.want"))
        val onlyOne = listOf(review(1, "pron.i"))
        assertTrue(PhraseUnlock.open(listOf(recipe), PhraseUnlock.learnedIds(1, AppLanguage.En, onlyOne)).isEmpty())
        val both = onlyOne + review(1, "verb.want")
        assertEquals(
            listOf("cmp.l1.i_want"),
            PhraseUnlock.open(listOf(recipe), PhraseUnlock.learnedIds(1, AppLanguage.En, both)).map { it.conceptId },
        )
    }

    @Test
    fun englishAnswersDoNotOpenVietnamese() {
        val recipe = PhraseRecipe("cmp.l2.i_want_water", 2, setOf("pron.i", "verb.want", "food.water"))
        val reviews = listOf("pron.i", "verb.want", "food.water").map { review(1, it, AppLanguage.En) } +
            review(1, "pron.i", AppLanguage.Vi)
        val ready = PhraseUnlock.ready(
            listOf(recipe),
            profileId = 1,
            targets = listOf(AppLanguage.En, AppLanguage.Vi),
            reviews = reviews,
            now = now,
        )
        assertEquals(listOf(AppLanguage.En), ready.map { it.targetLang })
    }

    @Test
    fun levelDoesNotGateALaterSentence() {
        val hard = PhraseRecipe("cmp.l3.hot", 3, setOf("pron.i", "verb.want", "adj.hot", "food.coffee"))
        val learned = hard.wordIds.map { review(1, it) }
        val open = PhraseUnlock.open(listOf(hard), PhraseUnlock.learnedIds(1, AppLanguage.En, learned))
        assertEquals(listOf("cmp.l3.hot"), open.map { it.conceptId })
    }

    @Test
    fun emptyUsesNeverOpen() {
        val recipe = PhraseRecipe("cmp.empty", 1, emptySet())
        assertTrue(PhraseUnlock.open(listOf(recipe), setOf("pron.i")).isEmpty())
    }

    @Test
    fun otherProfileDoesNotUnlock() {
        val recipe = PhraseRecipe("cmp.l1.i_want", 1, setOf("pron.i", "verb.want"))
        val reviews = listOf(review(2, "pron.i"), review(2, "verb.want"))
        val ready = PhraseUnlock.ready(listOf(recipe), 1, listOf(AppLanguage.En), reviews, now)
        assertTrue(ready.isEmpty())
    }

    @Test
    fun scheduledPhraseIsNotReadyUntilDue() {
        val recipe = PhraseRecipe("cmp.l1.i_want", 1, setOf("pron.i", "verb.want"))
        val words = listOf(review(1, "pron.i"), review(1, "verb.want"))
        val later = words + review(1, recipe.conceptId, dueAt = now + 50_000)
        assertTrue(PhraseUnlock.ready(listOf(recipe), 1, listOf(AppLanguage.En), later, now).isEmpty())
        val due = words + review(1, recipe.conceptId, dueAt = now)
        assertEquals(1, PhraseUnlock.ready(listOf(recipe), 1, listOf(AppLanguage.En), due, now).size)
    }
}
