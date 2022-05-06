package com.example.maparequestubicaciones

import com.google.gson.Gson
import java.util.*

data class Usuario(var nombre:String, var password:String, var token:String, var fecha: Calendar, var listaRutas:List<Ruta>) {
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}