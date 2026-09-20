package dev.sergey.triad.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.domain.AppLanguage
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsController @Inject constructor(
    @ApplicationContext context: Context,
) {
    private var engine: TextToSpeech? = null
    private var ready = false

    init {
        engine = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
    }

    fun available(lang: AppLanguage): Boolean {
        val tts = engine ?: return false
        if (!ready) return false
        val locale = locale(lang)
        val result = tts.isLanguageAvailable(locale)
        return result >= TextToSpeech.LANG_AVAILABLE
    }

    fun speak(text: String, lang: AppLanguage): Boolean {
        val tts = engine ?: return false
        if (!available(lang)) return false
        tts.language = locale(lang)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
        return true
    }

    fun shutdown() {
        engine?.shutdown()
        engine = null
        ready = false
    }

    private fun locale(lang: AppLanguage) = when (lang) {
        AppLanguage.En -> Locale.US
        AppLanguage.Ru -> Locale.forLanguageTag("ru-RU")
        AppLanguage.Vi -> Locale.forLanguageTag("vi-VN")
    }
}
