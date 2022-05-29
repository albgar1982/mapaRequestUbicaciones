package com.example.maparequestubicaciones

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.maparequestubicaciones.databinding.ItemRutasBinding

class SeleccionRutaAdapter(var rutas: List<String>,var usuario: String, var token : String) : RecyclerView.Adapter<SeleccionRutaAdapter.TextoViewHolder>(){

    class TextoViewHolder(var itemBinding : ItemRutasBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun getItemCount(): Int {
        return rutas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextoViewHolder {
        val binding = ItemRutasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TextoViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TextoViewHolder, position: Int) {
        //var cont=0
        println("Desde el adapter. En la posición hay ${rutas[position]}")
        holder.itemBinding.tvNombre.text = rutas[position]

        holder.itemBinding.layoutPrincipal.setOnClickListener {
            MainActivity.launch(holder.itemBinding.root.context, rutas[position],usuario,token)
        }

    }
}