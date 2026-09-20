package dev.sergey.triad.i18n

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class I18nKeysTest {
    @Test
    fun stringKeysMatchAcrossLocales() {
        val res = resDir()
        val en = names(File(res, "values/strings.xml"))
        val ru = names(File(res, "values-ru/strings.xml"))
        val vi = names(File(res, "values-vi/strings.xml"))
        assertEquals(en, ru)
        assertEquals(en, vi)
        val enP = names(File(res, "values/plurals.xml"))
        assertEquals(enP, names(File(res, "values-ru/plurals.xml")))
        assertEquals(enP, names(File(res, "values-vi/plurals.xml")))
    }

    private fun names(file: File): Set<String> {
        val regex = Regex("""name="([^"]+)"""")
        return regex.findAll(file.readText()).map { it.groupValues[1] }.toSet()
    }

    private fun resDir(): File {
        val here = File(".").canonicalFile
        val candidates = listOf(
            File(here, "src/main/res"),
            File(here, "app/src/main/res"),
        )
        return candidates.first { it.exists() }
    }
}
