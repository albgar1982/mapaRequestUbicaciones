package com.example.maparequestubicaciones

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

//Para cuando hacemos getListRutas
@Parcelize
data class Rutas(val lista:List<String>):Parcelable{
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}
@Parcelize
data class RutaYProgreso(val nombre:String,val ubicaciones: List<Ubicacion>,val posic:Int):Parcelable{
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
data class Ubicacion(val nombreCoordenada : String,val latitud:Double,val longitud:Double,val pista:String):Parcelable{
    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)

    }
}

