package com.example.gaspricesnearme.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.gaspricesnearme.data.SettingsKeys
import com.example.gaspricesnearme.data.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    val searchRadiusFlow: Flow<Float> =
        context.settingsDataStore.data.map { preferences ->
            preferences[SettingsKeys.SEARCH_RADIUS] ?: 5f
        }

    suspend fun saveSearchRadius(radius: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.SEARCH_RADIUS] = radius
        }
    }
}