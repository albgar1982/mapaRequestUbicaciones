package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maparequestubicaciones.databinding.ActivityLoginBinding
import com.example.maparequestubicaciones.databinding.SeleccionRutaBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class SeleccionRutaActivity : AppCompatActivity(){
    private lateinit var binding: SeleccionRutaBinding
    private lateinit var  adapter:SeleccionRutaAdapter
    private val seleccionRutaViewModel: SeleccionRutaViewModel by viewModels()

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"

        fun launch(context: Context, token: String) {
            val intent = Intent(context, SeleccionRutaActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SeleccionRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Recuperamos del intent el valor del token:
        val token = intent.getStringExtra(TAG_TOKEN).toString()

        initObserver()
        binding.recyclerRutas.layoutManager = LinearLayoutManager(this)
        seleccionRutaViewModel.hacerLlamadaUsuario(token,this)
    }
    private fun  initObserver(){
        seleccionRutaViewModel.user.observe(this){
            val gson = Gson()
            val usuario = gson.fromJson(it, Usuario::class.java)


            println("Estoy en el initObserver de SeleccionRutaActivity. El usuario es $usuario este momento es el "+ System.currentTimeMillis())

            println("Ya he intentado lo del adapter. "+System.currentTimeMillis())
            mostrarRutas(usuario)
        }
    }
    private  fun mostrarRutas(usuario: Usuario){
        adapter=SeleccionRutaAdapter(usuario)
        binding.recyclerRutas.adapter=adapter
    }
}
