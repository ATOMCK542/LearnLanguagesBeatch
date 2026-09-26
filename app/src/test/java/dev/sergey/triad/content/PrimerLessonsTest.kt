package dev.sergey.triad.content

import dev.sergey.triad.data.content.PackParser
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.LessonBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PrimerLessonsTest {
    @Test
    fun eachLanguageHasWordBuildingAndSentences() {
        val langs = listOf("en", "ru", "vi")
        val files = listOf("en.json", "ru.json", "vi.json")
        files.forEach { name ->
            val theme = PackParser.themesOf(
                PackParser.parse("""{"theme": ${File(primers(), name).readText()}}"""),
            ).single()
            val ids = theme.sections.map { it.id }.toSet()
            assertTrue("$name missing words", "words" in ids)
            assertTrue("$name missing sentences", "sentences" in ids)
            assertEquals(name.substringBefore('.'), theme.primerLanguage()!!.code)
            theme.sections.forEach { section ->
                assertFilled(section.title.en, section.title.ru, section.title.vi, "$name ${section.id} title")
                assertTrue("$name ${section.id} empty", section.blocks.isNotEmpty())
                section.blocks.forEach { block ->
                    when (block) {
                        is LessonBlock.Paragraph ->
                            assertFilled(block.text.en, block.text.ru, block.text.vi, "$name ${section.id} text")
                        is LessonBlock.Speak -> {
                            assertTrue("$name speak lang", block.lang in AppLanguage.all)
                            assertTrue("$name empty say", block.say.isNotBlank())
                            assertFilled(block.caption.en, block.caption.ru, block.caption.vi, "$name ${section.id} caption")
                        }
                    }
                }
            }
            langs.forEach { code ->
                assertTrue(theme.description.forLang(AppLanguage.fromCode(code)).isNotBlank())
            }
        }
    }

    private fun assertFilled(en: String, ru: String, vi: String, where: String) {
        assertTrue("$where en", en.isNotBlank())
        assertTrue("$where ru", ru.isNotBlank())
        assertTrue("$where vi", vi.isNotBlank())
    }

    private fun primers(): File {
        val here = File(".").canonicalFile
        val candidates = listOf(here, here.parentFile, File(here, "app").parentFile)
        val root = candidates.first { File(it, "content-src/primers").isDirectory }
        return File(root, "content-src/primers")
    }
}
