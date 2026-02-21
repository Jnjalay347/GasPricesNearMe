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

    // Adds a single Map Pin
    fun addMapPin(
        latitude: Double,
        longitude: Double,
        svgFileName: String
    ) {

        val point =
            MapPoint.CreateFromGeoCoordinates(latitude, longitude)

        val bitmap = SVGRender.render(
            assets,
            svgFileName,
            SVGRender.transform(renderer.screenScale.toDouble())
        ) ?: return

        val marker = GLMapImage(100)

        marker.setBitmap(bitmap)
        marker.position = point
        marker.setOffset(bitmap.width / 2, bitmap.height)

        renderer.add(marker)
        markers.add(marker)

        bitmap.recycle()
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
}