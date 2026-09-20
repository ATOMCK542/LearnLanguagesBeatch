package dev.sergey.triad.data.tts

import dev.sergey.triad.domain.AppLanguage
import java.util.Locale

object TtsLocales {
    fun candidates(lang: AppLanguage): List<Locale> = when (lang) {
        AppLanguage.En -> listOf(Locale.US, Locale.UK, Locale.ENGLISH, Locale.forLanguageTag("en"))
        AppLanguage.Ru -> listOf(Locale.forLanguageTag("ru-RU"), Locale("ru"), Locale.forLanguageTag("ru"))
        AppLanguage.Vi -> listOf(Locale.forLanguageTag("vi-VN"), Locale("vi"), Locale.forLanguageTag("vi"))
    }

    fun matches(locale: Locale, lang: AppLanguage): Boolean =
        locale.language.equals(lang.code, ignoreCase = true)
}
