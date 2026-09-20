package dev.sergey.triad.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("triad_prefs")

@Singleton
class ActiveProfileStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = longPreferencesKey("activeProfileId")

    val activeProfileId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[key]?.takeIf { it > 0L }
    }

    suspend fun get(): Long? = activeProfileId.first()

    suspend fun set(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id == null || id <= 0L) prefs.remove(key) else prefs[key] = id
        }
    }
}
