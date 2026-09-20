package dev.sergey.triad.data.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class UiClickWaveTest {
    @Test
    fun wavIsShortQuietPcm() {
        val pcm = UiClickWave.pcm16()
        assertEquals(UiClickWave.SAMPLE_RATE * UiClickWave.DURATION_MS / 1_000, pcm.size)
        assertTrue(pcm.size < UiClickWave.SAMPLE_RATE / 20)
        assertTrue(pcm.any { it != 0.toShort() })
        val peak = pcm.maxOf { abs(it.toInt()) }
        assertTrue(peak > 0)
        assertTrue(peak < Short.MAX_VALUE * 0.35)
        val wav = UiClickWave.wavBytes()
        assertArrayEquals("RIFF".toByteArray(), wav.copyOfRange(0, 4))
        assertArrayEquals("WAVE".toByteArray(), wav.copyOfRange(8, 12))
        assertEquals(44 + pcm.size * 2, wav.size)
    }
}
