package com.kiberkot.dailysync.model

import java.util.UUID

// Catálogo sintético de Estado de Ánimo (Lemac DataLab)
data class CategoriaAnimo(
    val id: Int,
    val nombre: String
)

// Entidad principal del registro de ánimo
data class RegistroAnimo(
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "usr_demo_01", // Usuario ficticio de prueba
    val categoriaId: Int,
    val categoriaNombre: String,
    val fechaHora: String, // Ej: "2026-10-02 18:00"
    val nivelIntensidad: Int, // Escala 1 a 5
    val contexto: String, // Ej: "Estudio", "Hogar", "Trabajo", "Social"
    val notaOpcional: String? = null,
    val pendienteSync: Boolean = true
)