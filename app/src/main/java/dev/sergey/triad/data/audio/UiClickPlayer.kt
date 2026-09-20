package dev.sergey.triad.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiClickPlayer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val pool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    @Volatile private var soundId = 0
    @Volatile private var loaded = false

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            loaded = status == 0 && sampleId == soundId
        }
        val file = File(context.cacheDir, "triad_ui_click.wav")
        file.writeBytes(UiClickWave.wavBytes())
        soundId = pool.load(file.absolutePath, 1)
    }

    fun play() {
        if (!loaded) return
        if (audio?.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        pool.play(soundId, VOLUME, VOLUME, 1, 0, 1f)
    }

    companion object {
        private const val VOLUME = 0.28f
    }
}
