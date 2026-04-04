package com.example.gaspricesnearme

import android.content.res.AssetManager
import globus.glmap.GLMapImage
import globus.glmap.GLMapViewRenderer
import globus.glmap.MapPoint
import globus.glmap.SVGRender

class MapPinMarker(
    private val renderer: GLMapViewRenderer,
    private val assets: AssetManager
) {

    private val markers = mutableListOf<GLMapImage>()
    private var currentLocationMarker: GLMapImage? = null

    // Adds a single Map Pin
    fun addMapPin(
        latitude: Double,
        longitude: Double,
        svgFileName: String,
        scale: Double = 1.0
    ): GLMapImage? {

        val point =
            MapPoint.CreateFromGeoCoordinates(latitude, longitude)

        val bitmap = SVGRender.render(
            assets,
            svgFileName,
            SVGRender.transform(renderer.screenScale.toDouble() * scale)
        ) ?: return null

        val marker = GLMapImage(100)

        marker.setBitmap(bitmap)
        marker.position = point
        // Center offset for circle, bottom-center for pin
        if (svgFileName.contains("circle")) {
            marker.setOffset(bitmap.width / 2, bitmap.height / 2)
        } else {
            marker.setOffset(bitmap.width / 2, bitmap.height)
        }

        renderer.add(marker)
        markers.add(marker)

        bitmap.recycle()
        return marker
    }

    // Updates or adds the "Current Location" marker
    fun updateCurrentLocationMarker(
        latitude: Double?,
        longitude: Double?,
        svgFileName: String,
        scale: Double = 1.0
    ) {
        // Remove existing marker if it exists
        currentLocationMarker?.let {
            renderer.remove(it)
            markers.remove(it)
            currentLocationMarker = null
        }

        // Add new marker if coordinates are provided
        if (latitude != null && longitude != null) {
            currentLocationMarker = addMapPin(latitude, longitude, svgFileName, scale)
        }
    }

    // Adds multiple Map Pins
    fun addMultipleMapPins(
        locations: List<Pair<Double, Double>>,
        svgFileName: String
    ) {
        locations.forEach { (lat, lon) ->
            addMapPin(lat, lon, svgFileName)
        }
    }

    fun removeAllMarkers() {
        markers.forEach { renderer.remove(it) }
        markers.clear()
        currentLocationMarker = null
    }
}