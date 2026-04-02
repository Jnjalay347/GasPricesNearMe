package com.example.gaspricesnearme.repository

import com.example.gaspricesnearme.model.GasStation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for fetching gas stations via Firestore...
 * AND saves selection as user's favorite ("Set Favorite Gas Station" feature)
*/
class GasStationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun searchStations(query: String): List<GasStation> {
        return try {
            val snapshot = firestore.collection("stations")
                .whereGreaterThanOrEqualTo("stationName", query)
                .whereLessThanOrEqualTo("stationName", query + "\uf8ff")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(GasStation::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveFavoriteStation(userId: String, station: GasStation) {
        val data = mapOf(
            "favoriteStationName" to station.stationName,
            "favoriteStationAddress" to station.address,
            "favoriteLatitude" to station.latitude,
            "favoriteLongitude" to station.longitude
        )

        firestore.collection("users")
            .document(userId)
            .set(data)
            .await()
    }
}