package dev.sergey.triad.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguageTest {
    @Test
    fun parsesKnownCodes() {
        assertEquals(AppLanguage.En, AppLanguage.fromCode("EN"))
        assertEquals(AppLanguage.Ru, AppLanguage.fromCode("ru"))
        assertEquals(AppLanguage.Vi, AppLanguage.fromCode("vi"))
        assertEquals(3, AppLanguage.all.size)
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsUnknown() {
        AppLanguage.fromCode("zh")
    }
}
