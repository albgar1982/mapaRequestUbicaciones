package com.example.maparequestubicaciones

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng

class StreetViewActivity : AppCompatActivity(), OnStreetViewPanoramaReadyCallback {

    companion object {
        // George St, Sydney
        private val SYDNEY = LatLng(-33.87365, 151.20689)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.street_view_panorama_basic_demo)

        val streetViewPanoramaFragment =
            supportFragmentManager.findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment?

        streetViewPanoramaFragment?.getStreetViewPanoramaAsync { panorama ->
            // Only set the panorama to SYDNEY on startup (when no panoramas have been
            // loaded which is when the savedInstanceState is null).
            savedInstanceState ?: panorama.setPosition(SYDNEY)
        }
    }



    override fun onStreetViewPanoramaReady(p0: StreetViewPanorama) {
        val sanFrancisco = LatLng(37.754130, -122.447129)
        p0.setPosition(sanFrancisco)
    }
}