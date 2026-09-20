package dev.sergey.triad.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IpaCoverageTest {
    @Test
    fun everyConceptHasEnglishIpaForAllLanguages() {
        val packs = File(projectRoot(), "content-src/packs").listFiles { f -> f.extension == "json" }!!
        var counted = 0
        packs.forEach { file ->
            val blob = file.readText()
            val windows = blob.split("\"id\": \"").drop(1)
            windows.forEach { window ->
                if (!window.contains("\"texts\"")) return@forEach
                counted += 1
                listOf("en", "ru", "vi").forEach { lang ->
                    val langSlice = window.substringAfter("\"$lang\": {", missingDelimiterValue = "")
                    assertTrue("${file.name} missing $lang form", langSlice.isNotBlank())
                    val ipaLine = langSlice.lineSequence().firstOrNull { it.contains("\"ipa\"") }.orEmpty()
                    assertTrue("${file.name} missing $lang ipa: $ipaLine", ipaLine.contains("\"ipa\": \"/"))
                    assertFalse("${file.name} empty $lang ipa", ipaLine.contains("\"ipa\": \"\"") || ipaLine.contains("\"ipa\": \"/\""))
                }
            }
        }
        assertTrue("expected packed concepts", counted > 1000)
    }

    @Test
    fun greetingHelloUsesRealIpaNotOrthography() {
        val greetings = File(projectRoot(), "content-src/packs/80_greetings.json").readText()
        assertTrue(greetings.contains("həˈləʊ") || greetings.contains("həˈloʊ"))
        assertTrue(greetings.contains("prʲɪˈvʲet"))
        assertFalse(greetings.contains("\"ipa\": \"/hello/\""))
        assertFalse(greetings.contains("\"ipa\": \"\""))
    }

    @Test
    fun atLeastThousandUniqueFormsPerLanguage() {
        val packs = File(projectRoot(), "content-src/packs").listFiles { f -> f.extension == "json" }!!
        val uniques = mapOf("en" to mutableSetOf<String>(), "ru" to mutableSetOf(), "vi" to mutableSetOf())
        var concepts = 0
        packs.forEach { file ->
            val blob = file.readText()
            blob.split("\"id\": \"").drop(1).forEach { window ->
                if (!window.contains("\"texts\"")) return@forEach
                concepts += 1
                listOf("en", "ru", "vi").forEach { lang ->
                    val slice = window.substringAfter("\"$lang\": {")
                    val textLine = slice.lineSequence().firstOrNull { it.contains("\"text\"") }.orEmpty()
                    val value = textLine.substringAfter("\"text\": \"").substringBefore("\"").lowercase()
                    if (value.isNotBlank()) uniques.getValue(lang).add(value)
                }
            }
        }
        assertTrue("concepts=$concepts", concepts >= 1000)
        uniques.forEach { (lang, set) ->
            assertTrue("$lang unique=${set.size}", set.size >= 1000)
        }
    }

    private fun projectRoot(): File {
        val here = File(".").canonicalFile
        val candidates = listOf(here, here.parentFile, File(here, "app").parentFile)
        return candidates.first { File(it, "content-src/en-closed-class.md").exists() }
    }
}
