package com.example.gaspricesnearme
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    fun syncStationToLocal(
        context: Context,
        onOK: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("stations")
            .get()
            .addOnSuccessListener { snap ->
                val stations = snap.documents.map { doc ->
                    StationEntity(
                        coordinates = doc.id,
                        address = doc.getString("address") ?: "",
                        stationName = doc.getString("stationName") ?: "",
                        prices = doc.getString("prices") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0
                    )
                }

                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val dao = AppDatabase.getInstance(context).stationDao()
                        dao.clear()
                        dao.upsertAll(stations)
                        withContext(Dispatchers.Main) {onOK}
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { onError(e)}
                    }
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }
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
            "address" to address.trim(),
            "prices" to prices,
            "rating" to rating,
            "uid" to user.uid,
            "createAt" to FieldValue.serverTimestamp()
        )

        db.collection("reports")
            .add(data)
            .addOnSuccessListener { onOK() }
            .addOnFailureListener { e -> onError(e) }
    }
}
