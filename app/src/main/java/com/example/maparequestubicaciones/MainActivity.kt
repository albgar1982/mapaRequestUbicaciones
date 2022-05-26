package com.example.maparequestubicaciones

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"
        const val REQUEST_CODE_LOCATION = 0

        fun launch(context: Context, ruta: String, user: String, token: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            intent.putExtra(TAG_USER, user)
            intent.putExtra(TAG_RUTA, ruta)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var token: String
    private lateinit var usuario: String
    private lateinit var ruta: String
    private var listaUbicaciones = mutableListOf<Ubicacion>()
    lateinit var map: GoogleMap
    private val viewModel: MainActivityViewModel by viewModels()

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // ?.tag = 0 es para contar cuántas veces se ha pulsado la ubicación (se llama a onMarkerClick() automáticamente)
        listaUbicaciones.forEach {
            googleMap.addMarker(MarkerOptions().position(LatLng(it.latitud,it.longitud)).title(it.nombreCoordenada))?.tag = 0
        }

        val ultimaCoordenada = listaUbicaciones[listaUbicaciones.size - 1]
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

        //Cuando el mapa se cree, vamos a comprobar si están los permisos y a intentar localizar al usuario
        enableLocation()
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        StreetViewActivity.launch(this,ruta,usuario,Ubicacion("",marker.position.latitude,marker.position.longitude,"").toString())
        return false
        /*
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

         */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recuperamos del intent el valor de la ruta, del usuario y del token:
        ruta = intent.getStringExtra(TAG_RUTA).toString()
        token = intent.getStringExtra(TAG_TOKEN).toString()
        usuario = intent.getStringExtra(TAG_USER).toString()

        //Ponemos el nombre del usuario debajo del icono de logros
        binding.tvNombreUsuario.text = usuario.uppercase()

        //Para que aparezca y desaparezca la pista actual
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

        initObserver()
        viewModel.hacerLlamadaProgreso(usuario,ruta,token,this)
    }

    private fun initObserver() {
        viewModel.isVisible.observe(this) { isVisible ->
            if (isVisible)
                setVisible()
            else
                setGone()
        }

        viewModel.responseText.observe(this) {

            val gson = Gson()
            val rutaYprogreso = gson.fromJson(it, RutaYProgreso::class.java)

            rutaYprogreso.listaUbicaciones.forEach { ubi ->
                println(ubi)
                listaUbicaciones += gson.fromJson(ubi.toString(),Ubicacion::class.java)
            }

            binding.pista.text= rutaYprogreso.listaUbicaciones[rutaYprogreso.pistaActual].pista

            //Una vez que tenemos toda la información de la llamada, colocamos el maps (esto disparará el onMapReady() )
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment

            mapFragment.getMapAsync(this)
        }
    }

    private fun setVisible() {
        binding.pbDownloading.visibility = View.VISIBLE
    }

    private fun setGone() {
        binding.pbDownloading.visibility = View.GONE
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

}