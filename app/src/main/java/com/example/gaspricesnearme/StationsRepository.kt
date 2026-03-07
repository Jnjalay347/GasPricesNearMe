package com.example.gaspricesnearme

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StationsRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncStationsToLocal(appDb: AppDatabase) {
        val snapshot = firestore.collection("stations").get().await()

        val stations = snapshot.documents.mapNotNull { doc ->
            val coordinates = doc.id

            val address = doc.getString("address") ?: return@mapNotNull null
            val stationName = doc.getString("stationName") ?: return@mapNotNull null
            val prices = doc.getString("prices") ?: return@mapNotNull null
            val rating = doc.getDouble("rating") ?: 0.0

            StationEntity(
                coordinates = coordinates,
                address = address,
                stationName = stationName,
                prices = prices,
                rating = rating
            )
        }

        val dao = appDb.stationDao()
        dao.clear()
        dao.upsertAll(stations)
    }
}