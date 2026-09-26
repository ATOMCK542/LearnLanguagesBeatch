package dev.sergey.triad.data.db

import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.ConceptKind
import dev.sergey.triad.domain.ConceptText
import dev.sergey.triad.domain.FsrsCardState
import dev.sergey.triad.domain.LessonBlock
import dev.sergey.triad.domain.LessonSection
import dev.sergey.triad.domain.LocalizedText
import dev.sergey.triad.domain.Profile
import dev.sergey.triad.domain.ReviewItem
import dev.sergey.triad.domain.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {
    @Test
    fun profileRoundTrip() {
        val profile = Profile(
            3, "Ann", "a@b.c", 1, 2, AppLanguage.Vi, AppLanguage.Vi,
            listOf(AppLanguage.En, AppLanguage.Ru), 12, false, 4, "2026-01-01", 9,
        )
        val back = with(Mappers) { profile.toEntity().toDomain() }
        assertEquals(profile, back)
    }

    @Test
    fun conceptRoundTrip() {
        val concept = Concept(
            "id1",
            ConceptKind.Phrase,
            "cafe",
            listOf("theme:cafe"),
            LocalizedText("e", "r", "v"),
            mapOf(
                AppLanguage.En to ConceptText(AppLanguage.En, "hello", "h", mapOf(AppLanguage.Ru to "хелло"), emptyList()),
                AppLanguage.Ru to ConceptText(AppLanguage.Ru, "привет", "", emptyMap(), emptyList()),
                AppLanguage.Vi to ConceptText(AppLanguage.Vi, "xin chào", "", mapOf(AppLanguage.En to "sin chao"), listOf("ngang", "hỏi")),
            ),
        )
        val entity = with(Mappers) { concept.toEntity() }
        val texts = concept.texts.values.map { with(Mappers) { it.toEntity(concept.id) } }
        val back = Mappers.assembleConcept(entity, texts)
        assertEquals(concept.id, back.id)
        assertEquals("hello", back.text(AppLanguage.En).text)
        assertEquals("хелло", back.text(AppLanguage.En).hints[AppLanguage.Ru])
        assertEquals(listOf("ngang", "hỏi"), back.text(AppLanguage.Vi).tones)
        assertEquals("r", back.grammar.ru)
    }

    @Test
    fun themeAndReviewRoundTrip() {
        val theme = Theme("t", "unit", 4, LocalizedText("A", "Б", "C"), LocalizedText("d", "е", "f"))
        val backTheme = with(Mappers) { theme.toEntity().toDomain() }
        assertEquals(theme, backTheme)
        val withLessons = Theme(
            "primer_en",
            "primer",
            2,
            LocalizedText("English", "Английский", "Tiếng Anh"),
            LocalizedText("Sounds", "Звуки", "Âm"),
            listOf(
                LessonSection(
                    "sentences",
                    LocalizedText("Sentences", "Предложения", "Câu"),
                    listOf(
                        LessonBlock.Paragraph(LocalizedText("Order", "Порядок", "Thứ tự")),
                        LessonBlock.Speak(AppLanguage.En, "Do you read?", LocalizedText("do", "do", "do")),
                    ),
                ),
            ),
        )
        val backLessons = with(Mappers) { withLessons.toEntity().toDomain() }
        assertEquals(withLessons, backLessons)
        val plain = ThemeEntity(
            "cafe",
            "unit",
            1,
            """{"en":"Cafe","ru":"Кафе","vi":"Quán"}""",
            """{"en":"d","ru":"е","vi":"f"}""",
        )
        val backPlain = with(Mappers) { plain.toDomain() }
        assertEquals("е", backPlain.description.ru)
        assertTrue(backPlain.sections.isEmpty())
        val review = ReviewItem(1, "c", AppLanguage.En, 2.0, 4.0, 9, 3, 2, 1, FsrsCardState.Review, 1.5, 2.0)
        val backReview = with(Mappers) { review.toEntity().toDomain() }
        assertEquals(review, backReview)
        val primer = with(Mappers) { PrimerProgressEntity(1, "primer_vi", true).toDomain() }
        assertEquals("primer_vi", primer.primerId)
        val path = with(Mappers) { PathProgressEntity(1, "cafe", true, 3).toDomain() }
        assertEquals(3, path.completedCount)
    }
}
