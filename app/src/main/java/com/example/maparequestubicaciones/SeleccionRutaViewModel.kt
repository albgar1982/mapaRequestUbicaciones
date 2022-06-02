package com.example.maparequestubicaciones

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SeleccionRutaViewModel : ViewModel() {
    private val _isVisible by lazy { MediatorLiveData<Boolean>() }
    val isVisible: LiveData<Boolean>
        get() = _isVisible

    suspend fun setIsVisibleInMainThread(value: Boolean) = withContext(Dispatchers.Main) {
        _isVisible.value = value
    }

    private val _user by lazy { MediatorLiveData<String>() }
    val user: LiveData<String>
        get() = _user

    suspend fun setResponseTextInMainThread(value: String) = withContext(Dispatchers.Main) {
        _user.value = value
    }

    fun hacerLlamadaRutas(token: String, context: Context) {
        viewModelScope.launch {


            withContext(Dispatchers.IO) { //En un hilo secundario
                setIsVisibleInMainThread(true)
                val client = OkHttpClient()
                val request = Request.Builder()
                request.url("https://b66f-139-47-74-123.eu.ngrok.io/getListRutas")
                println("LLamando para conseguir las rutas...")
                val call = client.newCall(request.build())

                call.enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("Alberto", "Error en el onFailure")
                        println(e.toString())
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call, response: Response) {
                        response.body?.let { responseBody ->

                            val body = responseBody.string()
                            //Debería llegar un string con las rutas
                            println(body)

                            CoroutineScope(Dispatchers.Main).launch {
                                setIsVisibleInMainThread(false)
                                setResponseTextInMainThread(body)
                            }
                        }
                    }
                })
            }
        }
    }
}