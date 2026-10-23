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
    val pendienteSync: Boolean
)

data class FormularioDbt(
    val habilidadId: Int? = null,
    val situacion: String = "",
    val intensidad: Int = 3,
    val utilidad: Int = 3
) {
    fun errores(habilidadesDisponibles: Set<Int>): Map<String, String> = buildMap {
        if (habilidadId == null || habilidadId !in habilidadesDisponibles) {
            put("habilidad", "Selecciona una habilidad del catálogo.")
        }
        if (situacion.trim().length !in 3..250) {
            put("situacion", "Escribe entre 3 y 250 caracteres.")
        }
        if (intensidad !in 1..5) put("intensidad", "La intensidad debe estar entre 1 y 5.")
        if (utilidad !in 1..5) put("utilidad", "La utilidad debe estar entre 1 y 5.")
    }
}
