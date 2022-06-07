package com.example.maparequestubicaciones

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class LoginActivityViewModel : ViewModel() {
    var token = ""

    private val _falloPassword by lazy { MediatorLiveData<String>() }
    val falloPassword: LiveData<String>
        get() = _falloPassword

    suspend fun setResponseTextInMainThread(value : String) = withContext(Dispatchers.Main){
        _falloPassword.value = value
    }

    fun cifrarPassword(password:String,text:String,sharedPreferences:SharedPreferences,usuario:String):String{
        if(text == "Vacio"){ //Si ese usuario NO tiene llave guardada...
            //...creo una.
            var llave=""
            repeat(4){
                llave += Random.nextInt(0,10).toString()
            }
            //La uso para cifrar la contraseña
            val cifrada=cifrar(password, llave)

            //Y guardo la llave en SharedPreferences
            val preferenciasEditables = sharedPreferences.edit()
            preferenciasEditables.putString(usuario, llave)
            preferenciasEditables.apply()

            return cifrada //Devuelvo esa contraseña cifrada
        }
        else //Si es un usuario que ya existía, cifro la contraseña con su llave y la devuelvo. Así el servidor dirá si es la buena o no
            return cifrar(password, text) //...devuelvo esa contraseña cifrada
    }
    fun hacerLlamadaRegistro(contraseniaCifrada : String,usuario: String,context: Context) {
        val client = OkHttpClient()
        val request = Request.Builder()
        request.url("https://4a9c-139-47-74-123.eu.ngrok.io/loguear/$usuario")
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = contraseniaCifrada.toRequestBody(mediaType)
        println("Envío en el requestBody la contra cifrada: $contraseniaCifrada")
        request.post(requestBody)
        val call = client.newCall(request.build())

        call.enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                println(e.toString())
                println("Fallo en la llamada al servidor.")
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {

                response.body?.let { responseBody ->
                    val body = responseBody.string()
                    println("Esta es la respuesta del servidor: $body")
                    if(body=="Contrasenia incorrecta")
                        //Se manda esa cadena al LoginActivity, para ponerlo en el EditText y que lo lea el usuario
                        CoroutineScope(Dispatchers.Main).launch {
                            setResponseTextInMainThread(body)
                        }

                    else{ //Si el usuario es nuevo o si ya existía y la contraseña es buena, me viene un token. Lanzo la nueva activity y le meto ese token
                        this@LoginActivityViewModel.token = body
                        println("Este es el token que me ha devuelto el servidor: "+token)
                        println("Lanzo la SeleccionRutaActivity en ESTE punto")
                        SeleccionRutaActivity.launch(context,usuario,token)
                    }
                }
            }
        })
    }
}
private fun cifrar(contra: String, llave: String): String {
    println("Voy a cifrar: $contra usando la llave: $llave")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, getKey(llave))
    val texto = cipher.doFinal(contra.toByteArray(Charsets.UTF_8))
    println("texto = $texto")
    val cifrado = android.util.Base64.encodeToString(texto,android.util.Base64.URL_SAFE)
    println("He obtenido $cifrado")
    return cifrado

}

private fun getKey(llaveEnString : String): SecretKeySpec {
    var llaveUtf8 = llaveEnString.toByteArray(Charsets.UTF_8)
    val sha = MessageDigest.getInstance("SHA-1")
    llaveUtf8 = sha.digest(llaveUtf8)
    llaveUtf8 = llaveUtf8.copyOf(16)
    return SecretKeySpec(llaveUtf8, "AES")
}
