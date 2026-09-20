package dev.sergey.triad.data.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object UiClickWave {
    const val SAMPLE_RATE = 22_050
    const val DURATION_MS = 16
    const val PEAK = 0.20

    fun pcm16(): ShortArray {
        val n = SAMPLE_RATE * DURATION_MS / 1_000
        val out = ShortArray(n)
        val scale = PEAK * Short.MAX_VALUE
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 140.0)
            val tick = sin(2 * PI * 2_150 * t) * 0.72 + sin(2 * PI * 3_400 * t) * 0.28
            out[i] = (tick * env * scale).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    fun wavBytes(): ByteArray {
        val pcm = pcm16()
        val dataBytes = pcm.size * 2
        val buffer = ByteBuffer.allocate(44 + dataBytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
        buffer.putInt(36 + dataBytes)
        buffer.put(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))
        buffer.put(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(1)
        buffer.putInt(SAMPLE_RATE)
        buffer.putInt(SAMPLE_RATE * 2)
        buffer.putShort(2)
        buffer.putShort(16)
        buffer.put(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
        buffer.putInt(dataBytes)
        pcm.forEach { sample -> buffer.putShort(sample) }
        return buffer.array()
    }
}
