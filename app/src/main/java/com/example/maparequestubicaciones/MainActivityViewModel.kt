package com.example.maparequestubicaciones

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

class MainActivityViewModel : ViewModel() {

    private val _isVisible by lazy { MediatorLiveData<Boolean>() }
    val isVisible: LiveData<Boolean>
        get() = _isVisible

    private val _responseProgress by lazy { MediatorLiveData<String>() }
    val responseText: LiveData<String>
        get() = _responseProgress

    suspend fun setIsVisibleInMainThread(value: Boolean) = withContext(Dispatchers.Main) {
        _isVisible.value = value
    }

    suspend fun setResponseTextInMainThread(value: String) = withContext(Dispatchers.Main) {
        _responseProgress.value = value
    }

    fun hacerLlamadaProgreso(usuario: String, ruta: String, token: String, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setIsVisibleInMainThread(true)

                val client = OkHttpClient()
                val request = Request.Builder()
                request.url("https://b5b5-139-47-74-123.eu.ngrok.io/getProgress/$usuario/$ruta/$token")

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

                            val body = responseBody.string()
                            println("Estoy en el body: $body")

                            if (body == "error")
                                LoginActivity.launch(context)
                            else
                                CoroutineScope(Dispatchers.Main).launch {
                                    setIsVisibleInMainThread(false)
                                    _responseProgress.value=body
                                    setResponseTextInMainThread(body)
                                }
                        }
                    }
                })
            }
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


