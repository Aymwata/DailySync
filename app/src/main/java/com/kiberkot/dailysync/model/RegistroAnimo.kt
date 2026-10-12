package com.kiberkot.dailysync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Catálogo sintético de Estado de Ánimo
data class CategoriaAnimo(
    val id: Int,
    val nombre: String
)

// Entidad principal adaptada para la tabla de Room (Persistencia Local)
@Entity(tableName = "registro_animo")
data class RegistroAnimo(
    @PrimaryKey
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