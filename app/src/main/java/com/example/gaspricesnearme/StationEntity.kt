package com.example.gaspricesnearme

import android.location.Address
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val coordinates: String,
    val address: String,
    val stationName: String,
    val prices: String,
    val rating: Double
)