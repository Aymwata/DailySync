package com.kiberkot.dailysync.model

import java.util.UUID

// Catálogo sintético de Estado de Ánimo (Lemac DataLab)
data class CategoriaAnimo(
    val id: Int,
    val nombre: String
)

// Modelo de dominio para la capa de UI y ViewModel
data class RegistroAnimo(
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "usr_demo_01",
    val categoriaId: Int,
    val categoriaNombre: String,
    val fechaHora: String,
    val nivelIntensidad: Int,
    val contexto: String,
    val notaOpcional: String? = null,
    val pendienteSync: Boolean = true
)