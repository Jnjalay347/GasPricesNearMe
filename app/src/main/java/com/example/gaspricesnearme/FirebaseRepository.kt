package com.example.gaspricesnearme

import androidx.compose.animation.core.snap
import androidx.compose.ui.layout.LayoutCoordinates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Station(
    val coordinates: String,
    val address: String,
    val stationName: String,
    val prices: String,
    val rating: Double
)

class FirebaseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun fetchStations(
        onResult: (List<Station>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("stations")
            .get()
            .addOnSuccessListener { snap ->
                val stations = snap.documents.map { doc ->
                    Station(
                        coordinates = doc.id,
                        address = doc.getString("address") ?: "",
                        stationName = doc.getString(("stationName")) ?: "",
                        prices = doc.getString("prices") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0
                    )
                }
                onResult(stations)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun submitReport(
        address: String,
        prices: String,
        rating: Int,
        onOK: () -> Unit,
        onError: (Exception) -> Unit
    ){
        val user =auth.currentUser
        if (user == null) {
            onError(IllegalStateException("Not sign in"))
            return
        }

        val data = hashMapOf(
            "prices" to prices,
            "rating" to rating,
            "uid" to user.uid,
            "createAt" to FieldValue.serverTimestamp()
        )

        db.collection("reports")
            .document(address)
            .set(data)
            .addOnSuccessListener { onOK() }
            .addOnFailureListener { e -> onError(e) }
    }
}
