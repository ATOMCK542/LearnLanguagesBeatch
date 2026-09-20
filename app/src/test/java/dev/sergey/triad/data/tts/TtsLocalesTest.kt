package dev.sergey.triad.data.tts

import dev.sergey.triad.domain.AppLanguage
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsLocalesTest {
    @Test
    fun vietnameseCandidatesUseViAndVietnam() {
        val tags = TtsLocales.candidates(AppLanguage.Vi).map { it.toLanguageTag() }
        assertTrue(tags.any { it.startsWith("vi") })
        assertTrue(tags.contains("vi-VN"))
    }

    @Test
    fun matchesIgnoresRegion() {
        assertTrue(TtsLocales.matches(Locale.forLanguageTag("vi-VN"), AppLanguage.Vi))
        assertTrue(TtsLocales.matches(Locale("vi"), AppLanguage.Vi))
        assertEquals(false, TtsLocales.matches(Locale.US, AppLanguage.Vi))
    }
}
