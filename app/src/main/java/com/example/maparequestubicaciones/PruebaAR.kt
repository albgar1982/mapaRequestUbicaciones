package com.example.maparequestubicaciones

import android.os.Bundle
import android.view.translation.TranslationManager
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.sin
import kotlin.math.cos

class PruebaAR : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pruebaar)

        /*
        val arFragment = supportFragmentManager.findFragmentById(R.id.arFragment)
        val sesion = Session(this)

        val translationManager = TranslationManager()
        sesion.createAnchor(Pose(Pose.makeTranslation(40.387947f, -3.734383f,2f)))
        */
        val sesion = Session(this)

        println("ESTÁ PERMITIDA LA GEOLOCALIZACIÓN ESPACIAL??? "+sesion.isGeospatialModeSupported(Config.GeospatialMode.ENABLED))


        val earth = sesion.earth
        earth?.let {
            // Values obtained by the Geospatial API are valid as long as the Earth object has TrackingState Tracking.
            if (it.trackingState == TrackingState.TRACKING) {
                // cameraGeospatialPose contains geodetic location, rotation, and confidences values.
                val cameraGeospatialPose = it.cameraGeospatialPose

                createAnchor(it,40.387947, -3.734383,cameraGeospatialPose.altitude,cameraGeospatialPose.heading)
            }
        }




    }

    /** Create an anchor at a specific geodetic location using a heading. */
    private fun createAnchor(earth : Earth ,latitude : Double , longitude : Double,altitude : Double,headingDegrees: Double) {
        // Convert a heading to a EUS quaternion:
        val angleRadians = Math.toRadians(180.0f - headingDegrees)
        val anchor = earth.createAnchor(latitude, longitude, altitude,0.0f,sin(angleRadians / 2).toFloat(),0.0f,cos(angleRadians / 2).toFloat())
    }
}