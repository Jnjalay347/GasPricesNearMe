package com.example.gaspricesnearme.repository

import com.example.gaspricesnearme.model.GasStation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
                val station = doc.toObject(GasStation::class.java)
                
                // Extract coordinates from document ID (Format: "latitude`longitude")
                val docId = doc.id
                val coordinates = docId.split("`")
                val lat = coordinates.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val lon = coordinates.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                
                // Return station with extracted coordinates populated
                station?.copy(latitude = lat, longitude = lon)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveFavoriteStation(userId: String, station: GasStation) {
        // Save the extracted coordinates separately into the user's document
        val data = mapOf(
            "favoriteStationName" to station.stationName,
            "favoriteStationAddress" to station.address,
            "favoriteLatitude" to station.latitude,
            "favoriteLongitude" to station.longitude
        )

        firestore.collection("users")
            .document(userId)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun getFavoriteStation(userId: String): GasStation? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data
                GasStation(
                    stationName = data?.get("favoriteStationName") as? String ?: "",
                    address = data?.get("favoriteStationAddress") as? String ?: "",
                    latitude = (data?.get("favoriteLatitude") as? Number)?.toDouble() ?: 0.0,
                    longitude = (data?.get("favoriteLongitude") as? Number)?.toDouble() ?: 0.0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}