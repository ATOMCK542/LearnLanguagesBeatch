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
        assertTrue(PackParser.themesOf(pack).first().sections.isEmpty())
        assertEquals(0, c.level)
        assertTrue(c.uses.isEmpty())
    }

    @Test
    fun parsesPhraseUses() {
        val concepts = PackParser.conceptsOf(
            PackParser.parse(
                """
                {"concepts":[{
                  "id":"cmp.l1.i_want","kind":"phrase","theme":"composed","level":1,
                  "uses":["pron.i","verb.want"],
                  "grammar":{"en":"I + want","ru":"я + хотеть","vi":"tôi + muốn"},
                  "texts":{
                    "en":{"text":"I want"},
                    "ru":{"text":"я хочу"},
                    "vi":{"text":"tôi muốn"}
                  }
                }]}
                """.trimIndent(),
            ),
        )
        val phrase = concepts.single()
        assertEquals(1, phrase.level)
        assertEquals(listOf("pron.i", "verb.want"), phrase.uses)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsSelfUse() {
        PackParser.conceptsOf(
            PackParser.parse(
                """
                {"concepts":[{
                  "id":"cmp.loop","kind":"phrase","theme":"composed","level":1,
                  "uses":["cmp.loop"],
                  "grammar":{"en":"","ru":"","vi":""},
                  "texts":{"en":{"text":"I want"},"ru":{"text":"я хочу"},"vi":{"text":"tôi muốn"}}
                }]}
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun parsesPrimerSectionsAndSpeakBlocks() {
        val pack = PackParser.parse(
            """
            {
              "theme": {
                "id": "primer_vi",
                "kind": "primer",
                "order": 0,
                "title": { "en": "Vietnamese", "ru": "Вьетнамский", "vi": "Tiếng Việt" },
                "description": { "en": "Sounds", "ru": "Звуки", "vi": "Âm" },
                "sections": [
                  {
                    "id": "words",
                    "title": { "en": "Words", "ru": "Слова", "vi": "Từ" },
                    "blocks": [
                      { "type": "text", "text": { "en": "Roots", "ru": "Корни", "vi": "Gốc" } },
                      {
                        "type": "speak",
                        "lang": "vi",
                        "say": "má",
                        "caption": { "en": "rising", "ru": "восходящий", "vi": "sắc" }
                      }
                    ]
                  }
                ]
              }
            }
            """.trimIndent(),
        )
        val theme = PackParser.themesOf(pack).single()
        assertEquals("primer_vi", theme.id)
        assertEquals(AppLanguage.Vi, theme.primerLanguage())
        val section = theme.sections.single()
        assertEquals("words", section.id)
        val paragraph = section.blocks[0] as dev.sergey.triad.domain.LessonBlock.Paragraph
        assertEquals("Корни", paragraph.text.ru)
        val spoken = section.blocks[1] as dev.sergey.triad.domain.LessonBlock.Speak
        assertEquals(AppLanguage.Vi, spoken.lang)
        assertEquals("má", spoken.say)
        assertEquals("sắc", spoken.caption.vi)
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
