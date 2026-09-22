package dev.sergey.triad.data.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.domain.WidgetDay
import dev.sergey.triad.domain.WidgetKind
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetDays by preferencesDataStore("widget_days")

@Singleton
class WidgetDayStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun isDone(
        profileId: Long,
        kind: WidgetKind,
        today: LocalDate = LocalDate.now(),
    ): Boolean {
        val stored = context.widgetDays.data.first()[key(profileId, kind)]
        return WidgetDay.isDone(stored, today.toString())
    }

    suspend fun markDone(
        profileId: Long,
        kind: WidgetKind,
        today: LocalDate = LocalDate.now(),
    ) {
        context.widgetDays.edit { prefs ->
            prefs[key(profileId, kind)] = today.toString()
        }
    }

    private fun key(profileId: Long, kind: WidgetKind) =
        stringPreferencesKey("done_${profileId}_${kind.name}")
}
