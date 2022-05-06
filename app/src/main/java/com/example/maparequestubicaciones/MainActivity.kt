package com.example.maparequestubicaciones

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.example.maparequestubicaciones.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

class MainActivity :  AppCompatActivity(),OnMapReadyCallback {



    companion object{

        private val TAG = MainActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5

        const val TAG_USER = "TAG_USER"

        fun launch(context: Context, usuario: String){
            val intent = Intent(context,MainActivity::class.java)
            intent.putExtra(TAG_USER,usuario)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel : MainActivityViewModel by viewModels()
    lateinit var manejadorLoc: LocationManager
    lateinit var proveedor: String
    private var lastKnownLocation: Location? = null
    private var cameraPosition: CameraPosition? = null
    lateinit var map: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val tagUser = intent.getStringExtra(TAG_USER)
        val gson = Gson()
        val user = gson.fromJson(tagUser,Usuario::class.java)

        binding.tvNombreUsuario.text=user.nombre

        var i=0
        while (!user.listaRutas[i].seleccionada)
            i++
        initObserver()

        iniciarMapa(user.listaRutas[i])
        //viewModel.hacerLlamada()
    }

    private fun initObserver() {
        viewModel.isVisible.observe(this) { isVisible ->
            if (isVisible)
                setVisible()
            else
                setGone()
        }

        viewModel.responseText.observe(this) {

            val listaUbicaciones = it.listaUbicaciones
            val primeraLatitud=listaUbicaciones[0].latitud
            println("Primera latitud:$primeraLatitud")

        }
    }

    private fun setVisible(){
        binding.pbDownloading.visibility = View.VISIBLE
    }
    private fun setGone(){
        binding.pbDownloading.visibility = View.GONE
    }

    private fun iniciarMapa(ruta : Ruta){
        //Manejador del servicio de localización
        manejadorLoc = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //Creamos un criterio según el cual...
        val criterio = Criteria().apply {
            isCostAllowed = false
            powerRequirement = Criteria.POWER_LOW
            //isAltitudeRequired=false
            //accuracy = Criteria.ACCURACY_FINE
            //TODO("Comparar la disponibilidad del GPS y Network(O de cualquier proveedor de localizaciones), para decidir cuál usar en cada caso")
        }
        //...buscamos el mejor proveedor...
        proveedor = manejadorLoc.getBestProvider(criterio, true).toString()
        //...y lo mostramos
        println("Mejor proveedor: $proveedor\n")

        println("Última localización conocida:")

        //Comprobamos si tenemos los permisos necesarios. Si no es así, los pedimos
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //Si no están autorizados los permisos, se piden:
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        //Intentamos acceder a la última localización a través del proveedor(Abrir antes el Maps y darle a ubicar, SI NO, NO FUNCIONARÁ)
        muestraLocaliz(manejadorLoc.getLastKnownLocation(proveedor)) //getLastKnownLocation() devuelve un Location, que es el parámetro que espera muestraLocaliz()



        manejadorLoc = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //Comprobamos si tenemos los permisos necesarios. Si no es así, los pedimos
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //Si no están autorizados los permisos, se piden:
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        // Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)
        mapFragment.getMapAsync { googleMap ->

            ruta.listaUbicaciones.forEach {
                val lat=it.latitud
                val long=it.longitud
                val latLng=LatLng(lat,long)
                googleMap.addMarker(MarkerOptions().position(latLng).title(it.nombreCoordenada)
                    .anchor(0.5F,0.5F)
                )
            }
            val ultimaCoordenada = ruta.listaUbicaciones[ruta.listaUbicaciones.size-1]
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(ultimaCoordenada.latitud,ultimaCoordenada.longitud)))
                 // googleMap.setMinZoomPreference(googleMap.maxZoomLevel)
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0F))
            googleMap.isBuildingsEnabled=true
            googleMap.isMyLocationEnabled=true

            googleMap.setOnMyLocationClickListener {
                println("${it.latitude} , ${it.longitude}, ${it.provider}")
            }
           /* ruta?.let {
                ubi1 =
                    LatLng(it.listaUbicaciones[0].latitud, it.listaUbicaciones[0].longitud)
                ubi2 =
                    LatLng(it.listaUbicaciones[1].latitud, it.listaUbicaciones[1].longitud)
                ubi3 =
                    LatLng(it.listaUbicaciones[2].latitud, it.listaUbicaciones[2].longitud)
                /*
                       Ubicacion("Plaza Benavente",40.4146,-3.7037,"",false),
                       Ubicacion("Plaza Santa Ana",40.4144,-3.7011,"",false),
                       Ubicacion("Antón Martín",40.4124,-3.6993,"",false)
                       */
                googleMap.addMarker(MarkerOptions().position(ubi1).title("Plaza Benavente"))
                googleMap.addMarker(MarkerOptions().position(ubi2).title("Plaza Santa Ana"))
                googleMap.addMarker(MarkerOptions().position(ubi3).title("Antón Martín"))
                println("UBICACION  UNOOOOO--------------\n${ubi1.latitude}")
            }


            println("ESTA LISTO EL MAPA?")
            println(mapaListo)*/


        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


    }
    private fun muestraLocaliz(localizacion: Location?) {
        if (localizacion == null)
            println("Localizacion desconocida\n")
        else
            println(localizacion.toString() + "\n")
    }



}