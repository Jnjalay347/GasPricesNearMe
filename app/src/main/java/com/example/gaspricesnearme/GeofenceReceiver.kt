package com.example.gaspricesnearme

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent?.hasError() == true) {
            Log.e("GeofenceReceiver", "Error code: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition

        // DWELL means they entered the radius and stayed there for 2 minutes
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val stationId = triggeringGeofences?.firstOrNull()?.requestId

            Log.d("GeofenceReceiver", "User is lingering at station coords: $stationId")

            // 1. Create an Intent to open the app when the notification is tapped
            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // We pass the coordinates so the app knows which station they are at
                putExtra("STATION_COORDS", stationId)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 2. Build the Notification
            val builder = NotificationCompat.Builder(context, "GEOFENCE_CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_dialog_map) // Standard Android map icon
                .setContentTitle("Gas Prices Near Me")
                .setContentText("Looks like you're at a gas station! Want to report the prices?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Attach the tap action
                .setAutoCancel(true) // Dismiss the notification when tapped

            // 3. Show the Notification (Checking permission for Android 13+)
            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Use a unique ID so multiple stations don't overwrite each other
                    val notificationId = stationId?.hashCode() ?: 1
                    notify(notificationId, builder.build())
                } else {
                    Log.e("GeofenceReceiver", "Missing POST_NOTIFICATIONS permission")
                }
            }
        }
    }
}