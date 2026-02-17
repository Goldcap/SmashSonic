package com.smashsonic.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smashsonic.data.model.BackgroundType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val backgroundKey = stringPreferencesKey("background_type")

    val backgroundType: Flow<BackgroundType> = context.dataStore.data.map { prefs ->
        val key = prefs[backgroundKey] ?: BackgroundType.SOLID_CYAN.key
        BackgroundType.fromKey(key)
    }

    suspend fun setBackgroundType(type: BackgroundType) {
        context.dataStore.edit { prefs ->
            prefs[backgroundKey] = type.key
        }
    }
}
