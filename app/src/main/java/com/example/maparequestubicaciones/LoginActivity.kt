package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.example.maparequestubicaciones.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginActivityViewModel by viewModels()

    var contraseniaCifrada= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObserver()

        var usuarioOk = false
        var contraOk = false

        binding.etUser.doOnTextChanged { text, start, before, count ->
            var algoDistinto = false

            text?.forEach { char ->
                if(!(char in 'a'..'z' || char in 'A'..'Z'))
                    algoDistinto = true
            }
            text?.let {
                usuarioOk = (it.length in 3..8 && !algoDistinto)
            }
            if (!usuarioOk)
                binding.tilUsuario.error = "Usuario no válido"
            else
                binding.tilUsuario.error = null
            binding.bSiguiente.isEnabled = (usuarioOk && contraOk)
        }

        binding.etContra.doOnTextChanged { text, start, before, count ->
            var algoDistinto = false
            var tieneLetras = false
            var tieneNumeros = false

            text?.forEach { char ->
                if (char in 'a'..'z' || char in 'A'..'Z')
                    tieneLetras = true
                else
                    if (char.code in 48..57)
                        tieneNumeros = true
                    else
                        algoDistinto = true

            }
            text?.let {
                contraOk = (it.length == 8 && tieneLetras && tieneNumeros && !algoDistinto)
                if (!contraOk)
                    binding.tilContra.error = "Contraseña no válida"
                else
                    binding.tilContra.error = null
            }
            binding.bSiguiente.isEnabled = (usuarioOk && contraOk)
        }

        binding.bSiguiente.setOnClickListener{
            val contra = binding.etContra.text.toString()
            val usuario = binding.etUser.text.toString()
            contraseniaCifrada=cifrarYguardarLlave(contra,usuario)
            //En contraseniaCifrada tendré una contraseña cifrada sí o sí, independientemente de si es una que no existía antes (de un
            //usuario nuevo) O la correcta de un usuario preexistente (al cifrarla con su llave, da la misma cifrada siempre)
            //O una incorrecta porque se ha usado la llave del usuario pero no da lo mismo porque ha metido mal la contraseña

            //Con ello, hacemos la llamada al servidor, que nos dirá si seguimos (devuelve el token renovado si ya existía el usuario y han acertado la contraseña
            // O si el usuario es nuevo) o devuelve "Contrasenia incorrecta" si el usuario existía pero han fallado la contraseña
            loginViewModel.hacerLlamadaRegistro(contraseniaCifrada,usuario,this)
        }
    }

    private fun initObserver(){
        loginViewModel.falloPassword.observe(this){
            binding.tvContra.visibility = View.VISIBLE
            binding.tvContra.text=it
        }
    }

    private fun cifrarYguardarLlave(contra : String, usuario:String) : String{

        //Busco la llave de ese usuario en SharedPreferences
        val sharedPreferences = getSharedPreferences("Llaves", Context.MODE_PRIVATE)
        val text = sharedPreferences.getString(usuario, "Vacio").toString()
        println(text)

        return loginViewModel.cifrarPassword(contra,text,sharedPreferences,usuario)
    }
}