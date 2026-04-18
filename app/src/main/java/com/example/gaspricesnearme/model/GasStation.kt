package com.example.gaspricesnearme.model

/**
 * Data class to represent how a user's favorite Gas Station...
 * is saved on Firestore (refer to "Users" section on Firebase)
 */
data class GasStation(
    val stationName: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val prices: String = ""
)