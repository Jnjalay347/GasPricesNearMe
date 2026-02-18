package com.example.gaspricesnearme

import android.app.Application
import android.content.res.AssetManager
import android.location.Location
import globus.glmap.GLMapAnimation
import globus.glmap.GLMapDrawable
import globus.glmap.GLMapImage
import globus.glmap.GLMapManager
import globus.glmap.GLMapVectorCascadeStyle
import globus.glmap.GLMapVectorLayer
import globus.glmap.GLMapVectorObject
import globus.glmap.GLMapViewRenderer
import globus.glmap.MapPoint
import globus.glmap.SVGRender
import kotlin.math.cos
import kotlin.math.sin


// ---------------------------------------------------------
// Maps Widget plugin 3-1
// ---------------------------------------------------------

class MapScreen : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize GLMap with your API key
        GLMapManager.Initialize(this, "b4da2cd3-a5ae-41ba-8f3b-4d963c49e8fe", null)
    }
}

//User Location Marker
class CurLocationHelper(private val renderer: GLMapViewRenderer) {
    private var userMovementImage: GLMapImage
    private var userLocationImage: GLMapImage
    private var accuracyCircle: GLMapVectorLayer
    private var lastLocation: Location? = null

    init {
        val manager = renderer.attachedView.context.assets
        userLocationImage = createImage(manager, "circle_new.svg")
        userMovementImage = createImage(manager, "arrow_new.svg")
        accuracyCircle = createAccuracyCircle()
    }

    private fun createImage(manager: AssetManager, filename: String): GLMapImage {
        val image = SVGRender.render(
            manager,
            filename,
            SVGRender.transform(renderer.screenScale.toDouble())
        ) ?: throw IllegalArgumentException("SVGRender can't render image")

        return GLMapImage(100).apply {
            setBitmap(image)
            isHidden = true
            setOffset(image.width / 2, image.height / 2)
            renderer.add(this)
            image.recycle()
        }
    }

    private fun createAccuracyCircle(): GLMapVectorLayer {
        val points = Array(100) {
            val f = 2 * Math.PI * it / 100
            MapPoint(sin(f) * 2048, cos(f) * 2048)
        }

        val circleStyle = GLMapVectorCascadeStyle.createStyle(
            "area{layer:100; width:1pt; fill-color:#3D99FA26; color:#3D99FA26;}"
        )!!

        val circle = GLMapVectorObject.createPolygon(arrayOf(points), null)

        return GLMapVectorLayer(99).apply {
            setTransformMode(GLMapDrawable.TransformMode.Custom)
            setVectorObject(circle, circleStyle, null)
            renderer.add(this)
        }
    }

    fun onLocationChanged(location: Location) {
        val position = MapPoint.CreateFromGeoCoordinates(location.latitude, location.longitude)

        // Accuracy radius
        val r = renderer.convertMetersToInternal(location.accuracy.toDouble()).toFloat()

        // Set initial position
        if (lastLocation == null) {
            lastLocation = location
            userMovementImage.position = position
            userLocationImage.position = position
            if (location.hasBearing()) userLocationImage.angle = -location.bearing
            accuracyCircle.position = position
            accuracyCircle.scale = r / 2048.0f.toDouble()
        }

        // Select what image to display
        if (location.hasBearing()) {
            userMovementImage.isHidden = false
            userLocationImage.isHidden = true
        } else {
            userLocationImage.isHidden = false
            userMovementImage.isHidden = true
        }

        // Animate position updates
        renderer.animate {
            it.setTransition(GLMapAnimation.Linear)
            it.setDuration(1.0)
            userMovementImage.position = position
            userLocationImage.position = position
            accuracyCircle.position = position
            accuracyCircle.scale = r / 2048.0f.toDouble()
            if (location.hasBearing()) userLocationImage.angle = -location.bearing
        }
    }
}