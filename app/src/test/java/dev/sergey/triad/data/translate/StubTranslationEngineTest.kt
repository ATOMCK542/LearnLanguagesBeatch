package dev.sergey.triad.data.translate

import dev.sergey.triad.domain.AppLanguage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StubTranslationEngineTest {
    @Test
    fun doesNotGoToNetwork() = runBlocking {
        val engine = StubTranslationEngine()
        assertFalse(engine.isReady())
        assertFalse(engine.capabilities.offline)
        val result = engine.translate("hi", AppLanguage.En, AppLanguage.Ru)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnsupportedOperationException)
    }
}
