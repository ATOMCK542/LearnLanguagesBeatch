package dev.sergey.triad.content

import dev.sergey.triad.data.content.PackParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PhraseRecipesTest {
    @Test
    fun composedPackLinksOnlyExistingWords() {
        val root = repoRoot()
        val packs = File(root, "content-src/packs")
        val vocab = packs.listFiles { file -> file.extension == "json" && file.name != "96_phrases.json" }
            .orEmpty()
            .flatMap { file ->
                PackParser.parse(file.readText()).concepts.map { it.id }
            }
            .toSet()
        val pack = PackParser.parse(File(packs, "96_phrases.json").readText())
        assertEquals("composed", pack.theme?.kind)
        val concepts = PackParser.conceptsOf(pack)
        assertEquals(36, concepts.size)
        assertEquals(12, concepts.count { it.level == 1 && it.uses.size == 2 })
        assertEquals(12, concepts.count { it.level == 2 && it.uses.size == 3 })
        assertEquals(12, concepts.count { it.level == 3 && it.uses.size >= 4 })
        concepts.forEach { phrase ->
            assertTrue(phrase.uses.isNotEmpty())
            assertTrue(phrase.id !in phrase.uses)
            assertTrue(phrase.uses.all { it in vocab })
            assertTrue(phrase.texts.values.all { it.text.split(Regex("\\s+")).size >= 2 })
        }
    }

    private fun repoRoot(): File {
        val here = File(".").canonicalFile
        val candidates = listOf(here, here.parentFile, File(here, "app").parentFile)
        return candidates.first { File(it, "content-src/packs/96_phrases.json").isFile }
    }
}
