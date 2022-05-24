package com.example.maparequestubicaciones
/*
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maparequestubicaciones.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.collections.MarkerManager

class MainActivity2 : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val TAG_USER = "TAG_USER"
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_RUTA = "TAG_RUTA"

        fun launch(context: Context, ruta: String, usuario: String,token:String) {
            val intent = Intent(context, MainActivity2::class.java)
            intent.putExtra(TAG_USER, usuario)
            intent.putExtra(TAG_TOKEN, token)
            intent.putExtra(TAG_RUTA, ruta)
            context.startActivity(intent)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {

        var markerSydney: Marker? = null
        val SYDNEY = LatLng(-33.87365, 151.20689)
        map = googleMap
        //Cuando el mapa se cree, vamos a comprobar si están los permisos y a intentar localizar al usuario
        enableLocation()
        markerSydney = map.addMarker(
            MarkerOptions()
                .position(SYDNEY)
                .title("Sidney")
        )
        markerSydney?.tag = 0
        val mManager = MarkerManager(googleMap)
        mManager.Collection().markers.add(markerSydney)
        //c
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        // Retrieve the data from the marker.
        val clickCount = marker.tag as? Int

        // Check if a click count was set, then display the click count.
        clickCount?.let {
            val newClickCount = it + 1
            marker.tag = newClickCount
            Toast.makeText(
                this,
                "${marker.title} has been clicked $newClickCount times.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }

    private lateinit var binding: ActivityMainBinding
    lateinit var map: GoogleMap

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usuarioJson = intent.getStringExtra(TAG_USER)
        val gson = Gson()
        val usuario = gson.fromJson(usuarioJson, Usuario::class.java)
        var pistaActual = ""
        var markerPrimero: Marker? = null
        var markerSegundo: Marker? = null
        var markerTercero: Marker? = null

        binding.tvNombreUsuario.text = usuario.nombre
        binding.goal.setImageResource(R.mipmap.goal)

        var i = 0
        while (!usuario.listaRutas[i].seleccionada)
            i++

        var j = 0
        var salir = false
        while (!salir) {
            if (!usuario.listaRutas[i].listaUbicaciones[j].coleccionado)
                salir = true
            else
                j++
        }

        binding.close.setOnClickListener {
            if (binding.pista.visibility == View.VISIBLE)
                binding.pista.visibility = View.GONE
            else
                binding.pista.visibility = View.VISIBLE

            if (binding.pista.visibility == View.GONE)
                binding.close.setImageResource(R.mipmap.uparrow)
            else
                binding.close.setImageResource(R.mipmap.cancel)


        }

        pistaActual = usuario.listaRutas[i].listaUbicaciones[j].pista

        binding.pista.text = pistaActual
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment

        val ruta = usuario.listaRutas[i]
        // Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)
        mapFragment.getMapAsync { googleMap ->

            markerPrimero = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            ruta.listaUbicaciones[0].latitud,
                            ruta.listaUbicaciones[0].longitud
                        )
                    )
                    .title(ruta.listaUbicaciones[0].nombreCoordenada)
            )
            markerPrimero?.tag = 0
            markerSegundo = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            ruta.listaUbicaciones[1].latitud,
                            ruta.listaUbicaciones[1].longitud
                        )
                    )
                    .title(ruta.listaUbicaciones[1].nombreCoordenada)
            )
            markerSegundo?.tag = 0
            markerTercero = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            ruta.listaUbicaciones[2].latitud,
                            ruta.listaUbicaciones[2].longitud
                        )
                    )
                    .title(ruta.listaUbicaciones[2].nombreCoordenada)
            )
            markerTercero?.tag = 0

            // Set a listener for marker click.
            //map.setOnMarkerClickListener(this)


            //googleMap.setOnMarkerClickListener()

            val ultimaCoordenada = ruta.listaUbicaciones[ruta.listaUbicaciones.size - 1]
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        ultimaCoordenada.latitud,
                        ultimaCoordenada.longitud
                    )
                )
            )
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0F))
            googleMap.isBuildingsEnabled = true
            googleMap.isMyLocationEnabled = true

            googleMap.setOnMyLocationClickListener {
                println("${it.latitude} , ${it.longitude}, ${it.provider}")
            }
        }
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun enableLocation() {

        //Si el mapa no está inicializado, sal
        if (!::map.isInitialized)
            return
        //Si se puede porque están los permisos, activa la localización
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else
        //Si no, llamamos a esta función
            requestLocationPermission()
    }

    private fun requestLocationPermission() {
        //Si no están los permisos porque los rechazó, se lo decimos
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Vaya a ajustes y acepte los permisos", Toast.LENGTH_LONG).show()
        } else
        //Si no, se los pedimos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
    }

    @SuppressLint("MissingSuperCall", "MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            //Si los acepta, activamos la localización
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else
            //Si no, le decimos que a qué aspira
                Toast.makeText(
                    this,
                    "Vaya a ajustes y acepte los permisos, que va a SER INCREÍBLE",
                    Toast.LENGTH_LONG
                ).show()

        }
    }


    private fun getUrl(origen: LatLng, destino: LatLng, modo: String): String {
        val cadOrigen = "origin=" + origen.latitude + "," + origen.longitude
        val cadDestino = "destination=" + destino.latitude + "," + destino.longitude
        val cadModo = "mode=$modo"
        val parametros = "$cadOrigen&$cadDestino&$cadModo"
        return "https://maps.googleapis.com/maps/api/directions/json?$parametros&key=" + getString(
            R.string.google_maps_key
        )
    }
}




*//**/