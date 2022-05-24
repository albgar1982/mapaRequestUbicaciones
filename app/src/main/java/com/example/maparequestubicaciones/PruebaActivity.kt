package com.example.maparequestubicaciones

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maparequestubicaciones.databinding.ActivityPruebaBinding

class PruebaActivity : AppCompatActivity() {

    companion object {
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_USER = "TAG_USER"
        const val TAG_RUTA = "TAG_RUTA"

        fun launch(context: Context, ruta: String) {
            val intent = Intent(context, PruebaActivity::class.java)
            intent.putExtra(TAG_RUTA, ruta)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityPruebaBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPruebaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val ruta = intent.getStringExtra(TAG_RUTA).toString()

        binding.tvPrueba.text = ruta
    }
}