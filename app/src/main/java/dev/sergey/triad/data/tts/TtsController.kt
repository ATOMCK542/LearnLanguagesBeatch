package dev.sergey.triad.data.tts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.domain.AppLanguage
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TtsController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val defaultEngine: TextToSpeech
    private var googleEngine: TextToSpeech? = null
    private val routes = linkedMapOf<AppLanguage, Route>()
    private val utterance = AtomicInteger()
    private val _status = MutableStateFlow(TtsStatus())
    val status: StateFlow<TtsStatus> = _status.asStateFlow()

    init {
        defaultEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                register(defaultEngine)
                maybeStartGoogle()
            } else {
                startGoogle()
            }
            publish()
        }
    }

    @Synchronized
    fun available(lang: AppLanguage): Boolean = routes.containsKey(lang)

    fun speak(text: String, lang: AppLanguage): Boolean {
        if (text.isBlank()) return false
        val route = synchronized(this) { routes[lang] } ?: return false
        route.voice?.let { route.engine.voice = it }
        route.engine.language = route.locale
        route.engine.setSpeechRate(SPEECH_RATE)
        val id = "triad-${lang.code}-${utterance.incrementAndGet()}"
        val result = route.engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
        return result == TextToSpeech.SUCCESS
    }

    fun openVoiceSettings() {
        val intents = listOf(
            Intent("com.android.settings.TTS_SETTINGS"),
            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA),
            Intent(Settings.ACTION_SETTINGS),
        )
        for (intent in intents) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: Exception) {
                continue
            }
        }
    }

    fun shutdown() {
        synchronized(this) {
            defaultEngine.shutdown()
            googleEngine?.shutdown()
            googleEngine = null
            routes.clear()
        }
        _status.value = TtsStatus()
    }

    private fun maybeStartGoogle() {
        if (defaultEngine.defaultEngine == GOOGLE_ENGINE) {
            publish()
            return
        }
        startGoogle()
    }

    private fun startGoogle() {
        googleEngine = TextToSpeech(context, { status ->
            if (status == TextToSpeech.SUCCESS) {
                googleEngine?.let(::register)
            }
            publish()
        }, GOOGLE_ENGINE)
    }

    @Synchronized
    private fun register(engine: TextToSpeech) {
        val preferGoogle = engine === googleEngine
        for (lang in AppLanguage.all) {
            val route = routeFor(engine, lang) ?: continue
            val current = routes[lang]
            if (current == null || (preferGoogle && lang == AppLanguage.Vi)) {
                routes[lang] = route
            }
        }
    }

    private fun routeFor(engine: TextToSpeech, lang: AppLanguage): Route? {
        val voices = installedVoices(engine)
        val offline = voices.firstOrNull { voice ->
            TtsLocales.matches(voice.locale, lang) &&
                !voice.isNetworkConnectionRequired &&
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
        }
        val anyLocal = offline ?: voices.firstOrNull { voice ->
            TtsLocales.matches(voice.locale, lang) &&
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
        }
        if (anyLocal != null) {
            return Route(engine, anyLocal.locale, anyLocal)
        }
        if (voices.isNotEmpty()) return null
        val locale = TtsLocales.candidates(lang).firstOrNull { candidate ->
            engine.isLanguageAvailable(candidate) >= TextToSpeech.LANG_AVAILABLE
        } ?: return null
        return Route(engine, locale, null)
    }

    private fun installedVoices(engine: TextToSpeech): List<Voice> = try {
        engine.voices?.toList().orEmpty()
    } catch (_: Exception) {
        emptyList()
    }

    private fun publish() {
        _status.value = TtsStatus(
            ready = true,
            voices = synchronized(this) { routes.keys.toSet() },
        )
    }

    private data class Route(
        val engine: TextToSpeech,
        val locale: Locale,
        val voice: Voice?,
    )

    companion object {
        const val GOOGLE_ENGINE = "com.google.android.tts"
        const val SPEECH_RATE = 0.88f
    }
}

data class TtsStatus(
    val ready: Boolean = false,
    val voices: Set<AppLanguage> = emptySet(),
)
