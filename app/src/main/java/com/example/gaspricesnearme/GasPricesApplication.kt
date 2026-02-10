package com.example.gaspricesnearme

import android.app.Application
import globus.glmap.GLMapManager

class GasPricesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize GLMap with your API key
        GLMapManager.Initialize(this, "b4da2cd3-a5ae-41ba-8f3b-4d963c49e8fe", null)
    }
}
