package dev.sergey.triad.content

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClosedClassChecklistTest {
    @Test
    fun everyChecklistIdHasThreeLanguages() {
        val root = projectRoot()
        val checklist = File(root, "content-src/en-closed-class.md")
        val ids = checklist.readLines().mapNotNull { line ->
            line.removePrefix("- ").trim().takeIf { line.startsWith("- ") && it.isNotBlank() }
        }
        assertTrue(ids.contains("q.whom"))
        assertTrue(ids.contains("aux.ought_to"))
        assertTrue(ids.contains("q.how_far"))
        assertTrue(ids.contains("pron.themselves"))
        assertTrue(ids.contains("aux.didnt"))
        val packs = File(root, "content-src/packs").listFiles { f -> f.extension == "json" }!!.toList()
        val blob = packs.joinToString("\n") { it.readText() }
        ids.forEach { id ->
            assertTrue("missing $id", blob.contains("\"id\": \"$id\""))
            val slice = blob.substringAfter("\"id\": \"$id\"")
            val window = slice.take(1200)
            assertTrue("$id missing en", window.contains("\"en\""))
            assertTrue("$id missing ru", window.contains("\"ru\""))
            assertTrue("$id missing vi", window.contains("\"vi\""))
        }
    }

    private fun projectRoot(): File {
        val here = File(".").canonicalFile
        val candidates = listOf(here, here.parentFile, File(here, "app").parentFile)
        return candidates.first { File(it, "content-src/en-closed-class.md").exists() }
    }
}
