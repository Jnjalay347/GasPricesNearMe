package com.example.gaspricesnearme

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceHelper(private val context: Context) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun setupGeofences(stations: List<StationEntity>) {
        if (stations.isEmpty()) return

        // Safety check to ensure we have background location permissions
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GeofenceHelper", "Missing background location permission.")
            return
        }

        val geofenceList = stations.mapNotNull { station ->
            val parts = station.coordinates.split("`")
            val lat = parts.getOrNull(0)?.toDoubleOrNull()
            val lon = parts.getOrNull(1)?.toDoubleOrNull()

            if (lat != null && lon != null) {
                Geofence.Builder()
                    .setRequestId(station.coordinates) // Use coordinates as the unique ID
                    .setCircularRegion(lat, lon, 50f) // 50 meter radius
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(120000) // Requires them to stay for 2 mins (120,000 ms) before triggering
                    .build()
            } else null
        }

        if (geofenceList.isEmpty()) return

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .addGeofences(geofenceList)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d("GeofenceHelper", "Successfully added ${geofenceList.size} geofences")
            }
            .addOnFailureListener { e ->
                Log.e("GeofenceHelper", "Failed to add geofences", e)
            }
    }
}