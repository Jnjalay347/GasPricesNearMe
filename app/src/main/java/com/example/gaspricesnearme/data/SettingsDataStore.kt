package com.example.gaspricesnearme.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val SEARCH_RADIUS = floatPreferencesKey("search_radius")
    val CURRENT_LOCATION = stringPreferencesKey("current_location")
}