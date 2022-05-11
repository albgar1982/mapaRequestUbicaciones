package com.example.maparequestubicaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

class MainActivityViewModel : ViewModel() {

    private val _isVisible by lazy { MediatorLiveData<Boolean>() }
    val isVisible: LiveData<Boolean>
        get() = _isVisible

    private val _responseRuta by lazy { MediatorLiveData<Ruta>() }
    val responseText : LiveData<Ruta>
        get() = _responseRuta

    suspend fun setIsVisibleInMainThread(value: Boolean) = withContext(Dispatchers.Main) {
        _isVisible.value = value
    }

    suspend fun setResponseTextInMainThread(value: Ruta) = withContext(Dispatchers.Main){
        _responseRuta.value = value
    }









    fun hacerLlamada() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setIsVisibleInMainThread(true)

                val client = OkHttpClient()
                val request = Request.Builder()
                request.url("https://adf4-139-47-74-123.eu.ngrok.io/getRuta/Benavente")

                val call = client.newCall(request.build())
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        CoroutineScope(Dispatchers.Main).launch {
                            println("Ha fallado la llamada.")
                            setIsVisibleInMainThread(false)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        println(response.toString())
                        response.body?.let { responseBody ->

                            val gson = Gson()
                            val body=responseBody.string()
                            println(body)

                            val ruta = gson.fromJson(body, Ruta::class.java)
                            CoroutineScope(Dispatchers.Main).launch {
                                setIsVisibleInMainThread(false)
                                _responseRuta.value=ruta
                                setResponseTextInMainThread(ruta)
                            }
                        }
                    }
                })
            }
        }
    }
   /* fun inicializarMaps(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync { }

                /* googleMap.addMarker(MarkerOptions().position(it).title("Estoy aquí")
                     .anchor(0.5F,0.5F)
                 )
                 googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
                 // googleMap.setMinZoomPreference(googleMap.maxZoomLevel)
             */
    }*/


}