package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewSource
import com.google.gson.Gson

class StreetViewActivity : AppCompatActivity(), OnStreetViewPanoramaReadyCallback {

    companion object {

        const val TAG_UBI = "TAG_UBI"
        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"

        fun launch(context: Context, ruta: String, user: String, ubi: String) {
            val intent = Intent(context, StreetViewActivity::class.java)
            intent.putExtra(TAG_UBI, ubi)
            intent.putExtra(TAG_USER, user)
            intent.putExtra(TAG_RUTA, ruta)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.street_view_panorama_basic_demo)

        val ubicacComienzoJson = intent.getStringExtra(TAG_UBI).toString()
        val gson = Gson()
        val ubicacComienzoUbi = gson.fromJson(ubicacComienzoJson,Ubicacion::class.java)
        val ubicacComienzoLatLng = LatLng(ubicacComienzoUbi.latitud,ubicacComienzoUbi.longitud)
        println("Ha llegado la siguiente ubicación: ${ubicacComienzoUbi.latitud},${ubicacComienzoUbi.longitud}")

        val streetViewPanoramaFragment =
            supportFragmentManager.findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment?

        streetViewPanoramaFragment?.getStreetViewPanoramaAsync { panorama ->
            savedInstanceState ?: panorama.setPosition(ubicacComienzoLatLng, StreetViewSource.OUTDOOR)
        }
    }

    override fun onStreetViewPanoramaReady(p0: StreetViewPanorama) {
        //Esto se usará si no hay permisos
        val sanFrancisco = LatLng(37.754130, -122.447129)
        p0.setPosition(sanFrancisco)
    }
}