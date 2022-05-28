package com.example.maparequestubicaciones

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

class PruebaARViewModel : ViewModel() {

    fun salvarProgreso(usuario: String, ruta: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val client = OkHttpClient()
                val request = Request.Builder()
                request.url("https://865b-139-47-74-123.eu.ngrok.io/salvarProgreso/$usuario/$ruta")

                val call = client.newCall(request.build())
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {


                    }
                })
            }
        }
    }
}