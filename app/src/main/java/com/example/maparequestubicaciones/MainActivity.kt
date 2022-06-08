package com.example.maparequestubicaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.maparequestubicaciones.databinding.ActivityMainBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"
        const val REQUEST_CODE_LOCATION = 0
        const val CANAL_ID = "mi canal"
        const val NOTIFICACION_ID = 1

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
    private lateinit var manejadorLoc: LocationManager
    var flag1 = false
    var flag2 = false
    var flag3 = false
    var localizacionActual: LatLng? = null
    var pistaActualInt = 0

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        //Cuando el mapa se cree, vamos a comprobar si están los permisos y a intentar localizar al usuario
        enableLocation()

        // ?.tag = 0 es para contar cuántas veces se ha pulsado la ubicación (se llama a onMarkerClick() automáticamente)
        listaUbicaciones.forEach {
            map.addMarker(
                MarkerOptions().position(LatLng(it.latitud, it.longitud)).title(it.nombreCoordenada)
            )?.tag = 0

            // Get back the mutable Circle
            map.addCircle(
                CircleOptions().center(LatLng(it.latitud, it.longitud)).radius(10.0)
                    .strokeWidth(10f).strokeColor(Color.GREEN)
            )
        }

        map.isBuildingsEnabled = true

        val ultimaCoordenada = listaUbicaciones[listaUbicaciones.size - 1]

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(
                ultimaCoordenada.latitud,
                ultimaCoordenada.longitud
            )) // Sets the center of the map to ultimaCoordenada
            //.bearing(90f)         // Sets the orientation of the camera to east
            .zoom(17f)
            .tilt(60f)            // Sets the tilt of the camera to 30 degrees
            .build()              // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var notificado = false

        manejadorLoc = getSystemService(LOCATION_SERVICE) as LocationManager

        enableLocation()
        conseguirLatLng()
        //Creo un manejador de notificaciones
        val notifMananager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //Si la versión es igual o superior a Oreo...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Creo un canal de notificaciones con un Id, nombre y nivel de importancia (hay varias constantes que hacen que )
            val canalNotif = NotificationChannel(
                CANAL_ID,
                "Mis notificationes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            canalNotif.description = "Descripción del canal"
            notifMananager.createNotificationChannel(canalNotif)
        }



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

        binding.logro.setOnClickListener {
            if (binding.layLogros.visibility == View.VISIBLE)
                binding.layLogros.visibility = View.GONE
            else
                binding.layLogros.visibility = View.VISIBLE
        }

        initObserver()
        viewModel.hacerLlamadaProgreso(usuario, ruta, token, this)


        GlobalScope.launch {
            while (true) {
                delay(3000)
                //Intentamos conseguir una localizacionActual:
                conseguirLatLng()
                println("LOCALIZACION ACTUAL: $localizacionActual")
                println("Se ha notificado?: $notificado")

                if (localizacionActual != null) {
                    //Comprobamos si el usuario está en el entorno de alguna de las ubicaciones de la ruta
                    flag1 =
                        comprobarDistancia(
                            localizacionActual!!,
                            LatLng(listaUbicaciones[0].latitud, listaUbicaciones[0].longitud)
                        )

                    flag2 = comprobarDistancia(
                        localizacionActual!!,
                        LatLng(listaUbicaciones[1].latitud, listaUbicaciones[1].longitud)
                    )
                    flag3 = comprobarDistancia(
                        localizacionActual!!,
                        LatLng(listaUbicaciones[2].latitud, listaUbicaciones[2].longitud)
                    )
                }

                //Si está cerca de alguna ubicación...
                if (flag1) {
                    //...comprobamos si es la buena, viendo si coinciden las pistas.
                    if (comprobarPista(
                            listaUbicaciones[0].pista,
                            listaUbicaciones[pistaActualInt].pista
                        )
                    ) {
                        //Si es así, lanzamos la actividad de AR, para colocar el regalo
                        ARActivity.launch(this@MainActivity, ruta, usuario, token,pistaActualInt)
                        return@launch
                    } else {
                        //Si no, lanzamos la notificación avisando de que ha ido al lugar erróneo:
                        if(!notificado) {
                            notificado=true
                            CoroutineScope(Dispatchers.Main).launch {
                                notificar(notifMananager)
                            }
                        }
                    }
                } else
                    if (flag2) {
                        if (comprobarPista(
                                listaUbicaciones[1].pista,
                                listaUbicaciones[pistaActualInt].pista
                            )
                        ) {
                            ARActivity.launch(this@MainActivity, ruta, usuario, token,pistaActualInt)
                            return@launch
                        } else {
                            if(!notificado) {
                                notificado=true
                                CoroutineScope(Dispatchers.Main).launch {

                                    notificar(notifMananager)

                                }
                            }
                        }
                    } else
                        if (flag3)
                            if (comprobarPista(
                                    listaUbicaciones[2].pista,
                                    listaUbicaciones[pistaActualInt].pista
                                )
                            ) {
                                ARActivity.launch(this@MainActivity, ruta, usuario, token,pistaActualInt)
                                return@launch
                            } else {
                                if(!notificado) {
                                    notificado=true
                                    CoroutineScope(Dispatchers.Main).launch {

                                        notificar(notifMananager)

                                    }
                                }
                            }
                //En cualquier momento, si el usuario deja de estar en el entorno de cualquier ubicación, ponemos el flag de notificaciones
                //como false, para que al volver a entrar en uno de esos entornos, se le pueda lanzar una notificación
                if (!flag1 && !flag2 && !flag3)
                    notificado=false
                println("FLAGS $flag1 , $flag2 , $flag3")
            }
        }

        println("LOCALIZACIÓN ACTUAL:   $localizacionActual")

    }

     @SuppressLint("RestrictedApi")
     fun notificar(notifMananager:NotificationManager){
        val notificacion =
            NotificationCompat.Builder(this@MainActivity, CANAL_ID)
                .setSmallIcon(R.mipmap.elf)
                .setContentTitle("UBICACIÓN ERRÓNEA")
                .setContentText("Desplázate a otra ubicación, ésa no es la correcta.")
                .setDefaults(Notification.DEFAULT_VIBRATE)
        notifMananager.notify(NOTIFICACION_ID, notificacion.build())

        if(notificacion.whenIfShowing  ==0L   )
            notifMananager.cancel(NOTIFICACION_ID)
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
            binding.cantRegalos.text = rutaYprogreso.llaves.toString()
            binding.cantRutas.text = rutaYprogreso.rutas.toString()

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

    @SuppressLint("MissingPermission")
    private fun conseguirLatLng() {
        if (isLocationPermissionGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && manejadorLoc.isProviderEnabled(
                    LocationManager.FUSED_PROVIDER
                )
            ) {
                val localizacion = manejadorLoc.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                localizacion?.let {
                    localizacionActual = LatLng(it.latitude, it.longitude)
                    println("LatLng conseguida por FUSED: $localizacionActual")
                }
            } else {
                if (manejadorLoc.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val localizacion =
                        manejadorLoc.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    localizacion?.let {
                        localizacionActual = LatLng(it.latitude, it.longitude)
                        println("LatLng conseguida por GPS: $localizacionActual")
                    }
                } else
                    if (manejadorLoc.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        val localizacion =
                            manejadorLoc.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        localizacion?.let {
                            localizacionActual = LatLng(it.latitude, it.longitude)
                            println("LatLng conseguida por NETWORK: $localizacionActual")
                        }
                    } else
                        println("No va ni el GPS ni el NETWORK cago en DIOS")

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

    fun comprobarDistancia(localizacionActual: LatLng, ubicacionAComprobar: LatLng): Boolean {

        return (SphericalUtil.computeDistanceBetween(
            localizacionActual,
            ubicacionAComprobar
        ) < 15.0)

    }

    fun comprobarPista(pistaAComprobar: String, pistaActual: String): Boolean {
        return (pistaAComprobar == pistaActual)
    }

    override fun getApplicationContext(): Context {
        return this
    }


}