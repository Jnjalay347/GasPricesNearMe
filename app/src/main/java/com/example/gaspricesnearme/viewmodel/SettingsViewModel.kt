package com.example.gaspricesnearme.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaspricesnearme.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _searchRadius = MutableStateFlow(5f)
    val searchRadius: StateFlow<Float> = _searchRadius.asStateFlow()

    init {
        viewModelScope.launch {
            repository.searchRadiusFlow.collect {
                _searchRadius.value = it
            }
        }
    }

    fun updateSearchRadius(radius: Float) {
        _searchRadius.value = radius
        viewModelScope.launch {
            repository.saveSearchRadius(radius)
        }
    }
}