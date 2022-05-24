package com.example.maparequestubicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maparequestubicaciones.databinding.SeleccionRutaBinding
import com.google.gson.Gson

class SeleccionRutaActivity : AppCompatActivity(){
    private lateinit var binding: SeleccionRutaBinding
    private lateinit var  adapter:SeleccionRutaAdapter
    private val seleccionRutaViewModel: SeleccionRutaViewModel by viewModels()
    private lateinit var token: String
    private lateinit var usuario: String

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"


        fun launch(context: Context,user:String, token: String) {
            val intent = Intent(context, SeleccionRutaActivity::class.java)
            intent.putExtra(TAG_TOKEN, token)
            intent.putExtra(TAG_USER, user)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SeleccionRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recuperamos del intent el valor del usuario y del token:
        token = intent.getStringExtra(TAG_TOKEN).toString()
        usuario = intent.getStringExtra(TAG_USER).toString()

        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        initObserver()
        seleccionRutaViewModel.hacerLlamadaRutas(token,this)
    }

    private fun  initObserver(){
        seleccionRutaViewModel.isVisible.observe(this) { isVisible ->
            if (isVisible)
                setVisible()
            else
                setGone()
        }

        seleccionRutaViewModel.user.observe(this){
            println("Al initObserver le llega esto: $it")
            val gson = Gson()
            val claseQueViene = gson.fromJson(it, Rutas::class.java)
            println(claseQueViene.toString())

            val rutas = claseQueViene.lista
            adapter=SeleccionRutaAdapter(rutas,usuario, token)
            binding.recyclerview.adapter=adapter

        }


    }

    private fun setVisible(){
        binding.pbDownloading.visibility = View.VISIBLE
    }
    private fun setGone(){
        binding.pbDownloading.visibility = View.GONE
    }
}
