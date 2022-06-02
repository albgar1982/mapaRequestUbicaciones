package com.example.maparequestubicaciones

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

class PruebaARViewModel : ViewModel() {

    fun salvarProgreso(usuario: String, ruta: String,context: Context) {
        println("Estoy en el PruebaARViewModel. Voy a hacer la llamada")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val client = OkHttpClient()
                val request = Request.Builder()
                request.url("https://b66f-139-47-74-123.eu.ngrok.io/salvarProgreso/$usuario/$ruta")

                val call = client.newCall(request.build())
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        CoroutineScope(Dispatchers.Main).launch {
                            println("Ha fallado la llamada.")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        println(response.toString())
                        response.body?.let { responseBody ->

                            val body = responseBody.string()
                            println("Estoy en el body de PruebaARViewModel. LLega esto de la REQUEST: $body")

                            if (body.contains("tokenParaSeleccion")) {
                                val gson = Gson()
                                val token=gson.fromJson(body,Token::class.java)
                                SeleccionRutaActivity.launch(context,usuario,token.tokenParaSeleccion)
                            }

                        }
                    }
                })
            }
        }
    }
}