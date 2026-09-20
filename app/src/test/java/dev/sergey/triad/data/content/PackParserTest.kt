package dev.sergey.triad.data.content

import dev.sergey.triad.domain.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackParserTest {
    private val json = """
        {
          "theme": {
            "id": "cafe",
            "kind": "unit",
            "order": 1,
            "title": { "en": "Cafe", "ru": "Кафе", "vi": "Quán" }
          },
          "concepts": [
            {
              "id": "cafe.i_want_coffee",
              "kind": "sentence",
              "theme": "cafe",
              "tags": ["theme:cafe"],
              "grammar": { "en": "want + noun", "ru": "want + сущ.", "vi": "want + danh từ" },
              "texts": {
                "en": { "text": "I want coffee", "ipa": "/aɪ/", "hints": { "ru": "ай", "vi": "ai" } },
                "ru": { "text": "Я хочу кофе", "ipa": "x", "hints": { "en": "ya", "vi": "ia" } },
                "vi": { "text": "Tôi muốn cà phê", "ipa": "t", "tones": ["ngang"], "hints": { "ru": "той", "en": "toy" } }
              }
            }
          ]
        }
    """.trimIndent()

    @Test
    fun parsesThreeLanguagesAndHints() {
        val pack = PackParser.parse(json)
        val concepts = PackParser.conceptsOf(pack)
        assertEquals(1, concepts.size)
        val c = concepts.first()
        assertEquals("I want coffee", c.text(AppLanguage.En).text)
        assertEquals("Я хочу кофе", c.text(AppLanguage.Ru).text)
        assertEquals("Tôi muốn cà phê", c.text(AppLanguage.Vi).text)
        assertFalse(c.text(AppLanguage.En).hints.containsKey(AppLanguage.En))
        assertEquals("ай", c.text(AppLanguage.En).hints[AppLanguage.Ru])
        assertEquals("/aɪ/", c.text(AppLanguage.En).ipa)
        assertEquals("want + сущ.", c.grammar.forLang(AppLanguage.Ru))
        assertEquals("Cafe", PackParser.themesOf(pack).first().title.en)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncompleteLanguages() {
        PackParser.conceptsOf(
            PackParser.parse(
                """
                {"concepts":[{"id":"x","kind":"word","theme":"t","grammar":{"en":"","ru":"","vi":""},"texts":{"en":{"text":"a"}}}]}
                """.trimIndent(),
            ),
        )
    }
}
