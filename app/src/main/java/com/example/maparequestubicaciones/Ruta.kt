package com.example.maparequestubicaciones

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rutas(val rutas:List<Ruta>):Parcelable{
    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)

    }
}
@Parcelize
data class Ruta(val nombre : String, val listaUbicaciones:List<Ubicacion>,
                var seleccionada:Boolean, val completada:Boolean):Parcelable{
    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)

    }
}
@Parcelize
data class Ubicaciones(val ubicaciones: List<Ubicacion>):Parcelable{
    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)

    }
}
@Parcelize
data class Ubicacion(val nombreCoordenada : String,val latitud:Double,val longitud:Double,val pista:String,val coleccionado:Boolean):Parcelable{
    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)

    }
}

