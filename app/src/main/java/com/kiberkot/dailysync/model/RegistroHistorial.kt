package com.kiberkot.dailysync.model

enum class AmbitoRegistro(val titulo: String) {
    ANIMO("Estado de Ánimo"),
    DBT("Habilidades DBT"),
    NEURODESARROLLO("Neurodesarrollo"),
    ADICCIONES("Adicciones")
}

data class RegistroHistorial(
    val id: String,
    val ambito: AmbitoRegistro,
    val titulo: String,
    val descripcion: String,
    val fechaHora: String,
    val intensidad: Int,
    val pendienteSync: Boolean
)