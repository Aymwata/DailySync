package com.kiberkot.dailysync.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo

data class AnimoUiState(
    val categorias: List<CategoriaAnimo> = listOf(
        CategoriaAnimo(1, "Tranquilo / Calmo"),
        CategoriaAnimo(2, "Motivado / Con energía"),
        CategoriaAnimo(3, "Ansioso / Inquieto"),
        CategoriaAnimo(4, "Abrumado / Estresado"),
        CategoriaAnimo(5, "Desanimado / Triste")
    ),
    val categoriaSeleccionada: CategoriaAnimo? = null,
    val nivelIntensidad: Float = 3f,
    val contexto: String = "",
    val notaOpcional: String = "",
    val errorCategoria: String? = null,
    val errorContexto: String? = null,
    val mensajeResultado: String? = null
)

class AnimoViewModel : ViewModel() {
    var uiState by mutableStateOf(AnimoUiState())
        private set

    // Lista observable en memoria (Semana 8)
    val historialAnimo = mutableStateListOf<RegistroAnimo>()

    fun seleccionarCategoria(categoria: CategoriaAnimo) {
        uiState = uiState.copy(categoriaSeleccionada = categoria, errorCategoria = null, mensajeResultado = null)
    }

    fun actualizarIntensidad(nuevaIntensidad: Float) {
        uiState = uiState.copy(nivelIntensidad = nuevaIntensidad)
    }

    fun actualizarContexto(nuevoContexto: String) {
        uiState = uiState.copy(contexto = nuevoContexto, errorContexto = null, mensajeResultado = null)
    }

    fun actualizarNota(nuevaNota: String) {
        uiState = uiState.copy(notaOpcional = nuevaNota)
    }

    // Retorna el ID creado si es válido, o null si hay errores
    fun guardarRegistro(): String? {
        var esValido = true
        var errCat: String? = null
        var errCtx: String? = null

        // Validación 1: Campo obligatorio
        if (uiState.categoriaSeleccionada == null) {
            errCat = "Debes seleccionar un estado de ánimo"
            esValido = false
        }

        // Validación 2: Regla de longitud mínima
        if (uiState.contexto.isBlank()) {
            errCtx = "El contexto o entorno es obligatorio"
            esValido = false
        } else if (uiState.contexto.trim().length < 3) {
            errCtx = "Usa al menos 3 caracteres para describir el contexto"
            esValido = false
        }

        if (!esValido) {
            uiState = uiState.copy(errorCategoria = errCat, errorContexto = errCtx)
            return null
        }

        val nuevoRegistro = RegistroAnimo(
            categoriaId = uiState.categoriaSeleccionada!!.id,
            categoriaNombre = uiState.categoriaSeleccionada!!.nombre,
            fechaHora = "2026-10-02 18:30",
            nivelIntensidad = uiState.nivelIntensidad.toInt(),
            contexto = uiState.contexto.trim(),
            notaOpcional = uiState.notaOpcional.ifBlank { null }
        )

        historialAnimo.add(nuevoRegistro)

        // Limpiar el formulario
        uiState = AnimoUiState(mensajeResultado = "Registro guardado exitosamente")
        return nuevoRegistro.id
    }

    // Búsqueda desacoplada por ID para la pantalla de Detalle
    fun buscarPorId(id: String): RegistroAnimo? {
        return historialAnimo.firstOrNull { it.id == id }
    }
}