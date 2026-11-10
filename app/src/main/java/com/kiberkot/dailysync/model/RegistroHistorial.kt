package com.kiberkot.dailysync.model


data class RegistroHistorial(
    val id: String,
    val ambito: AmbitoRegistro,
    val titulo: String,
    val descripcion: String,
    val fechaHora: String,
    val intensidad: Int,
    val pendienteSync: Boolean = true,
    val versionEscala: Int = 2,
    val editado: Boolean = false
)