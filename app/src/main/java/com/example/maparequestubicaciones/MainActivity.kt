package com.example.maparequestubicaciones

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maparequestubicaciones.databinding.ActivityMainBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*
import java.security.Provider

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
    private  lateinit var manager: LocationManager

    val loc1 = Location("loca1")
    val loc2 = Location("loca2")
    val loc3 = Location("loca3")

    var flag1 = false
    var flag2 = false
    var flag3 = false

    lateinit var localizacionActual:LatLng

    var pistaActualInt = 0

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // ?.tag = 0 es para contar cuántas veces se ha pulsado la ubicación (se llama a onMarkerClick() automáticamente)
        listaUbicaciones.forEach {
            map.addMarker(
                MarkerOptions().position(LatLng(it.latitud, it.longitud)).title(it.nombreCoordenada)
            )?.tag = 0

            // Get back the mutable Circle
            map.addCircle(
                CircleOptions().center(LatLng(it.latitud, it.longitud)).radius(25.0)
                    .strokeWidth(10f).strokeColor(Color.GREEN)
            )

        }

        loc1.latitude = listaUbicaciones[0].latitud
        loc1.longitude = listaUbicaciones[0].longitud

        loc2.latitude = listaUbicaciones[1].latitud
        loc2.longitude = listaUbicaciones[1].longitud

        loc3.latitude = listaUbicaciones[2].latitud
        loc3.longitude = listaUbicaciones[2].longitud

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


        var distancia = SphericalUtil.computeDistanceBetween(
            LatLng(listaUbicaciones[0].latitud, listaUbicaciones[0].longitud),
            LatLng(listaUbicaciones[1].latitud, listaUbicaciones[1].longitud)
        )

        println("La distancia entre ${listaUbicaciones[0].nombreCoordenada} y ${listaUbicaciones[1].nombreCoordenada} es de $distancia m")
        //Cuando el mapa se cree, vamos a comprobar si están los permisos y a intentar localizar al usuario
        enableLocation()
        //LocationSource.OnLocationChangedListener()
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {

       // PruebaARActivity.launch(this)
        //StreetViewActivity.launch(this,ruta,usuario,Ubicacion("",marker.position.latitude,marker.position.longitude,"").toString())
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


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission", "SetTextI18n")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manager=getSystemService(Context.LOCATION_SERVICE) as LocationManager


            GlobalScope.launch {
                while (true){

                    delay(3000)
                    requestLocationPermission()
                   if ( isLocationPermissionGranted())
                     localizacionActual= manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { LatLng(it.latitude,(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!.longitude))}!!
                     println("LOCALIZACIÓN ACTUAL:   $localizacionActual")
                    flag1 = comprobarDistancia(
                        localizacionActual,
                        LatLng(listaUbicaciones[0].latitud, listaUbicaciones[0].longitud)
                    )
                    flag2 = comprobarDistancia(
                        localizacionActual,
                        LatLng(listaUbicaciones[1].latitud, listaUbicaciones[1].longitud)
                    )
                    flag3 = comprobarDistancia(
                        localizacionActual,
                        LatLng(listaUbicaciones[2].latitud, listaUbicaciones[2].longitud)
                    )

                    if (flag1) {
                        if (comprobarPista(listaUbicaciones[0].pista, listaUbicaciones[pistaActualInt].pista)) {
                            PruebaARActivity.launch(this@MainActivity, ruta, usuario,token)
                            return@launch
                        }else{
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.pista.text = "Vete a otra puta ubicación gracias por tu dineros sucio de mierda flag 1"
                                println("LANZAR NOTIFICACIÓN")
                            }
                        }
                    } else
                        if (flag2) {
                            if (comprobarPista(listaUbicaciones[1].pista, listaUbicaciones[pistaActualInt].pista)) {
                                PruebaARActivity.launch(this@MainActivity, ruta, usuario,token)
                                return@launch
                            }else{
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.pista.text =
                                        "Vete a otra puta ubicación gracias por tu dineros sucio de mierda flag2"
                                    println("LANZAR NOTIFICACIÓN")
                                }
                            }
                        }else
                            if (flag3)
                                if (comprobarPista(listaUbicaciones[2].pista, listaUbicaciones[pistaActualInt].pista)) {
                                    PruebaARActivity.launch(this@MainActivity, ruta, usuario,token)
                                    return@launch
                                }else{
                                    CoroutineScope(Dispatchers.Main).launch {
                                        binding.pista.text =
                                            "Vete a otra puta ubicación gracias por tu dineros sucio de mierda flag3"
                                        println("LANZAR NOTIFICACIÓN")
                                    }
                                }
                    println("FLAGS $flag1 , $flag2 , $flag3")
                }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        println("COMPROBACION DE PROBVEFOR: ${manager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)}")
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
        viewModel.hacerLlamadaProgreso(usuario, ruta, token, this)
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
                listaUbicaciones += gson.fromJson(ubi.toString(), Ubicacion::class.java)
            }

            binding.pista.text = rutaYprogreso.listaUbicaciones[rutaYprogreso.pistaActual].pista
            pistaActualInt = rutaYprogreso.pistaActual
            //Una vez que tenemos toda la información de la llamada, colocamos el maps (esto disparará el onMapReady() )
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment

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

    fun comprobarDistancia(localizacionActual: LatLng, ubicacionAComprobar: LatLng): Boolean {

        return (SphericalUtil.computeDistanceBetween(localizacionActual, ubicacionAComprobar) < 26.0)

    }

    fun comprobarPista(pistaAComprobar: String, pistaActual: String): Boolean {
        return (pistaAComprobar == pistaActual)
    }


}