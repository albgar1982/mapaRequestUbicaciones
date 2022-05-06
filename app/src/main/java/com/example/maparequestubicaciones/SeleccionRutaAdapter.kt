package com.example.maparequestubicaciones

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.maparequestubicaciones.databinding.ItemRutasBinding

class SeleccionRutaAdapter(var usuario: Usuario) : RecyclerView.Adapter<SeleccionRutaAdapter.TextoViewHolder>(){

    class TextoViewHolder(var itemBinding : ItemRutasBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun getItemCount(): Int {
        return usuario.listaRutas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextoViewHolder {
        val binding = ItemRutasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TextoViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TextoViewHolder, position: Int) {
        var cont=0
        println("He llegado aqui")
        holder.itemBinding.tvNombreRuta.text=usuario.listaRutas[position].nombre
        usuario.listaRutas[position].listaUbicaciones.forEach {
            if (it.coleccionado)
                cont++
        }
        holder.itemBinding.llavesColeccionadas.text="${cont}/${usuario.listaRutas[position].listaUbicaciones.size}"

        holder.itemBinding.itemRutasLayout.setOnClickListener {
            usuario.listaRutas[position].seleccionada=true
            println("Usuario en adapter\n $usuario")
            MainActivity.launch(holder.itemBinding.root.context, usuario.toString())
        }
    }
}