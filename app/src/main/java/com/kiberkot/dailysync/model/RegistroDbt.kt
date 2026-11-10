package com.kiberkot.dailysync.model

data class RegistroDbt(
    val id: String,
    val usuarioId: String,
    val habilidadId: Int,
    val habilidadNombre: String,
    val modulo: String,
    val fechaHora: String,
    val situacion: String,
    val intensidad: Int,
    val utilidad: Int,
    val pendienteSync: Boolean,
    val versionEscala: Int = 2,
    val editado: Boolean = false
)

data class FormularioDbt(
    val habilidadId: Int? = null,
    val situacion: String = "",
    val intensidad: Int = 2,
    val utilidad: Int = 3
)
