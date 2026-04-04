package com.example.gaspricesnearme.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaspricesnearme.model.GasStation
import com.example.gaspricesnearme.repository.GasStationRepository
import com.example.gaspricesnearme.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages UI state and user's interaction related to...
 * settings, gas station searches, favorite gas station, etc.
 * on the Settings Screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val gasStationRepository = GasStationRepository()

    private val _searchRadius = MutableStateFlow(5f)
    val searchRadius: StateFlow<Float> = _searchRadius.asStateFlow()

    private val _currentLocation = MutableStateFlow<String?>(null)
    val currentLocation: StateFlow<String?> = _currentLocation.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GasStation>>(emptyList())
    val searchResults: StateFlow<List<GasStation>> = _searchResults

    private val _favoriteStation = MutableStateFlow<GasStation?>(null)
    val favoriteStation: StateFlow<GasStation?> = _favoriteStation

    init {
        viewModelScope.launch {
            repository.searchRadiusFlow.collect {
                _searchRadius.value = it
            }
        }
        viewModelScope.launch {
            repository.currentLocationFlow.collect {
                _currentLocation.value = it
            }
        }
    }

    fun updateSearchRadius(radius: Float) {
        _searchRadius.value = radius
        viewModelScope.launch {
            repository.saveSearchRadius(radius)
        }
    }

    fun updateCurrentLocation(location: String) {
        viewModelScope.launch {
            repository.saveCurrentLocation(location)
        }
    }

    fun clearCurrentLocation() {
        viewModelScope.launch {
            repository.clearCurrentLocation()
        }
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            val results = gasStationRepository.searchStations(query)
            _searchResults.value = results
        }
    }

    fun fetchFavoriteStation(userId: String) {
        viewModelScope.launch {
            val station = gasStationRepository.getFavoriteStation(userId)
            _favoriteStation.value = station
        }
    }

    fun saveFavoriteStation(userId: String, station: GasStation) {
        viewModelScope.launch {
            gasStationRepository.saveFavoriteStation(userId, station)
            _favoriteStation.value = station
        }
    }
}