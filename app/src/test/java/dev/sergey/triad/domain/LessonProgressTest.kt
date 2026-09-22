package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonProgressTest {
    private fun concept(id: String, theme: String) = Concept(
        id = id,
        kind = ConceptKind.Word,
        themeId = theme,
        tags = emptyList(),
        grammar = LocalizedText("", "", ""),
        texts = AppLanguage.all.associateWith { lang ->
            ConceptText(lang, id, "", emptyMap())
        },
    )

    private fun review(conceptId: String, reps: Int, lang: AppLanguage = AppLanguage.En) = ReviewItem(
        profileId = 1,
        conceptId = conceptId,
        targetLang = lang,
        stability = 1.0,
        difficulty = 5.0,
        dueAt = 0,
        lastReviewAt = 1,
        reps = reps,
        lapses = 0,
        state = FsrsCardState.Review,
        elapsedDays = 0.0,
        scheduledDays = 1.0,
    )

    @Test
    fun themeIsStudiedOnlyWhenEveryWordWasAnswered() {
        val concepts = listOf(concept("a", "cafe"), concept("b", "cafe"), concept("c", "home"))
        val partial = LessonProgress.byTheme(1, concepts, listOf(review("a", reps = 1)))
        assertEquals(ThemeStudy(1, 2), partial["cafe"])
        assertFalse(partial.getValue("cafe").studied)
        assertEquals(ThemeStudy(0, 1), partial["home"])

        val done = LessonProgress.byTheme(
            1,
            concepts,
            listOf(review("a", 1), review("b", 2), review("c", 1, AppLanguage.Vi)),
        )
        assertTrue(done.getValue("cafe").studied)
        assertTrue(done.getValue("home").studied)
    }

    @Test
    fun unseenCardsFromOtherProfilesAndZeroRepsStayPending() {
        val planned = listOf(
            review("a", 0),
            review("b", 0, AppLanguage.Vi),
            review("a", 0).copy(profileId = 2),
        )
        val existing = listOf(review("a", 1), review("b", 0))
        val pending = LessonProgress.stillUnseen(planned, existing)
        assertEquals(listOf("b" to AppLanguage.Vi, "a" to AppLanguage.En), pending.map { it.conceptId to it.targetLang })
        assertEquals(listOf(1L, 2L), pending.map { it.profileId })
    }
}
