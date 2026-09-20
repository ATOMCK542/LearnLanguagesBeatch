package dev.sergey.triad.data.locale

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dev.sergey.triad.domain.AppLanguage
import java.util.Locale

object AppLocale {
    fun apply(context: Context, lang: AppLanguage) {
        val tag = lang.code
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        if (Build.VERSION.SDK_INT >= 33) {
            val manager = context.getSystemService(LocaleManager::class.java)
            manager?.applicationLocales = LocaleList.forLanguageTags(tag)
        }
        Locale.setDefault(Locale.forLanguageTag(tag))
    }
}
