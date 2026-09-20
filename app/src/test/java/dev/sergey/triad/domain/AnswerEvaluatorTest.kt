package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.Normalizer

class AnswerEvaluatorTest {
    @Test
    fun trimsAndLowercases() {
        assertTrue(AnswerEvaluator.textMatches("Hello", "  HELLO ", AppLanguage.En))
    }

    @Test
    fun vietnameseUsesNfc() {
        val composed = "cà phê"
        val decomposed = Normalizer.normalize(composed, Normalizer.Form.NFD)
        assertTrue(AnswerEvaluator.textMatches(composed, decomposed, AppLanguage.Vi))
    }

    @Test
    fun chipsMustMatchOrder() {
        assertTrue(AnswerEvaluator.chipsMatch(listOf("I", "want"), listOf("I", "want"), AppLanguage.En))
        assertFalse(AnswerEvaluator.chipsMatch(listOf("I", "want"), listOf("want", "I"), AppLanguage.En))
    }

    @Test
    fun gameRatings() {
        assertEquals(Rating.Good, AnswerEvaluator.ratingForGame(true))
        assertEquals(Rating.Again, AnswerEvaluator.ratingForGame(false))
    }
}
