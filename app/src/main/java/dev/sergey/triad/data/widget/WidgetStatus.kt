package dev.sergey.triad.data.widget

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.R
import dev.sergey.triad.data.repo.TriadRepository
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.WidgetKind
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetCard(
    val kind: WidgetKind,
    val title: String,
    val status: String,
    val detail: String,
)

@Singleton
class WidgetStatus @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: TriadRepository,
    private val days: WidgetDayStore,
) {
    suspend fun load(kind: WidgetKind): WidgetCard {
        if (repo.themes().isEmpty()) repo.bootstrap()
        val profile = repo.activeProfile()
        val ui = localized(profile?.uiLang)
        val title = ui.getString(
            if (kind == WidgetKind.Lesson) R.string.widget_lesson_title else R.string.widget_review_title,
        )
        if (profile == null) {
            return WidgetCard(kind, title, ui.getString(R.string.widget_status_no_profile), "")
        }
        if (days.isDone(profile.id, kind)) {
            return WidgetCard(kind, title, ui.getString(R.string.widget_status_done), "")
        }
        val count = when (kind) {
            WidgetKind.Lesson -> repo.planSession(sessionSize = WidgetKind.SESSION_SIZE).items.size
            WidgetKind.Review -> repo.practicePoolSize().coerceAtMost(WidgetKind.SESSION_SIZE)
        }
        if (count == 0) {
            val empty = if (kind == WidgetKind.Lesson) {
                R.string.widget_status_empty_lesson
            } else {
                R.string.widget_status_empty_review
            }
            return WidgetCard(kind, title, ui.getString(empty), "")
        }
        return WidgetCard(
            kind,
            title,
            ui.getString(R.string.widget_status_ready),
            ui.resources.getQuantityString(R.plurals.widget_today, count, count),
        )
    }

    private fun localized(lang: AppLanguage?): Context {
        if (lang == null) return context
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(lang.code))
        return context.createConfigurationContext(config)
    }
}
