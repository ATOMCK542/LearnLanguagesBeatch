package dev.sergey.triad.data.unlock

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sergey.triad.domain.UnlockGate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.unlockStore by preferencesDataStore("triad_unlock")

data class UnlockPrefs(
    val enabled: Boolean = false,
    val requiredCorrect: Int = UnlockGate.DEFAULT_CORRECT,
)

@Singleton
class UnlockGateStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val prefs: Flow<UnlockPrefs> = context.unlockStore.data.map { stored ->
        UnlockPrefs(
            enabled = stored[enabledKey] ?: false,
            requiredCorrect = UnlockGate.clampRequired(stored[countKey] ?: UnlockGate.DEFAULT_CORRECT),
        )
    }

    suspend fun snapshot(): UnlockPrefs = prefs.first()

    suspend fun setEnabled(enabled: Boolean) {
        context.unlockStore.edit { it[enabledKey] = enabled }
    }

    suspend fun setRequiredCorrect(count: Int) {
        context.unlockStore.edit { it[countKey] = UnlockGate.clampRequired(count) }
    }

    private companion object {
        val enabledKey = booleanPreferencesKey("enabled")
        val countKey = intPreferencesKey("requiredCorrect")
    }
}
