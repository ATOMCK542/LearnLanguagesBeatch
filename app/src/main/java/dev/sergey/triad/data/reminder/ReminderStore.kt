package dev.sergey.triad.data.reminder

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.reminderStore by preferencesDataStore("triad_reminder")

data class ReminderPrefs(
    val enabled: Boolean = true,
    val prompted: Boolean = false,
)

@Singleton
class ReminderStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val prefs: Flow<ReminderPrefs> = context.reminderStore.data.map { stored ->
        ReminderPrefs(
            enabled = stored[enabledKey] ?: true,
            prompted = stored[promptedKey] ?: false,
        )
    }

    suspend fun snapshot(): ReminderPrefs = prefs.first()

    suspend fun setEnabled(enabled: Boolean) {
        context.reminderStore.edit { it[enabledKey] = enabled }
    }

    suspend fun markPrompted() {
        context.reminderStore.edit { it[promptedKey] = true }
    }

    suspend fun lastOpenDay(): String? = context.reminderStore.data.first()[openDayKey]

    suspend fun markOpened(day: String) {
        context.reminderStore.edit { it[openDayKey] = day }
    }

    suspend fun lastNotifiedDay(): String? = context.reminderStore.data.first()[notifiedDayKey]

    suspend fun markNotified(day: String) {
        context.reminderStore.edit { it[notifiedDayKey] = day }
    }

    private companion object {
        val enabledKey = booleanPreferencesKey("enabled")
        val promptedKey = booleanPreferencesKey("prompted")
        val openDayKey = stringPreferencesKey("openDay")
        val notifiedDayKey = stringPreferencesKey("notifiedDay")
    }
}
