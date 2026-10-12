package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.RegistroAnimoEntity
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.utils.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AnimoUiState(
    val categorias: List<CategoriaAnimo> = emptyList(),
    val historial: List<RegistroAnimo> = emptyList(),
    val categoriaSeleccionada: CategoriaAnimo? = null,
    val nivelIntensidad: Float = 3f,
    val contexto: String = "",
    val notaOpcional: String = "",
    val errorCategoria: String? = null,
    val errorContexto: String? = null,
    val bannerExitoVisible: Boolean = false
)

class AnimoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailySyncDao()
    private val notificationHelper = NotificationHelper(application)

    private val _uiState = MutableStateFlow(AnimoUiState())
    val uiState: StateFlow<AnimoUiState> = _uiState.asStateFlow()

    init {
        // Cargar Catálogo de Ánimo desde Room
        viewModelScope.launch {
            dao.obtenerCatalogoAnimo().collect { catalogoEntities ->
                val categorias = catalogoEntities.map { entity ->
                    CategoriaAnimo(id = entity.idCategoria, nombre = entity.nombreCategoria)
                }
                _uiState.update { it.copy(categorias = categorias) }
            }
        }

        // Cargar Historial de Registros de Ánimo desde Room
        viewModelScope.launch {
            dao.obtenerRegistrosAnimo().collect { entidades ->
                val catalogoMap = _uiState.value.categorias.associateBy { it.id }
                val registros = entidades.map { entity ->
                    RegistroAnimo(
                        id = entity.idRegistro,
                        usuarioId = entity.idUsuario,
                        categoriaId = entity.idCategoria,
                        categoriaNombre = catalogoMap[entity.idCategoria]?.nombre ?: "Ánimo #${entity.idCategoria}",
                        fechaHora = entity.fechaHora,
                        nivelIntensidad = entity.nivelIntensidad,
                        contexto = entity.contexto,
                        notaOpcional = entity.notaOpcional,
                        pendienteSync = entity.pendienteSync
                    )
                }
                _uiState.update { it.copy(historial = registros) }
            }
        }
    }

    fun seleccionarCategoria(categoria: CategoriaAnimo) {
        _uiState.update {
            it.copy(
                categoriaSeleccionada = categoria,
                errorCategoria = null
            )
        }
    }

    fun cambiarIntensidad(intensidad: Float) {
        _uiState.update { it.copy(nivelIntensidad = intensidad) }
    }

    fun cambiarContexto(contexto: String) {
        _uiState.update {
            it.copy(
                contexto = contexto,
                errorContexto = if (contexto.trim().length >= 3) null else it.errorContexto
            )
        }
    }

    fun cambiarNota(nota: String) {
        _uiState.update { it.copy(notaOpcional = nota) }
    }

    fun ocultarBanner() {
        _uiState.update { it.copy(bannerExitoVisible = false) }
    }

    fun guardarRegistro(): Boolean {
        val currentState = _uiState.value
        var esValido = true

        var errCategoria: String? = null
        var errContexto: String? = null

        if (currentState.categoriaSeleccionada == null) {
            errCategoria = "Debes seleccionar una categoría de estado de ánimo."
            esValido = false
        }

        if (currentState.contexto.trim().length < 3) {
            errContexto = "El contexto debe tener al menos 3 caracteres."
            esValido = false
        }

        if (!esValido) {
            _uiState.update {
                it.copy(
                    errorCategoria = errCategoria,
                    errorContexto = errContexto
                )
            }
            return false
        }

        val timestampActual = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val nuevoId = UUID.randomUUID().toString()

        val nuevaEntidad = RegistroAnimoEntity(
            idRegistro = nuevoId,
            idUsuario = "usr_demo_01",
            idCategoria = currentState.categoriaSeleccionada!!.id,
            fechaHora = timestampActual,
            nivelIntensidad = currentState.nivelIntensidad.toInt(),
            contexto = currentState.contexto.trim(),
            notaOpcional = currentState.notaOpcional.ifBlank { null },
            pendienteSync = true
        )

        viewModelScope.launch {
            // Guardar asíncronamente en Room DB
            dao.insertarRegistroAnimo(nuevaEntidad)

            // Emitir Notificación Nativa
            notificationHelper.mostrarNotificacionCheckIn(
                titulo = "Check-in Registrado",
                mensaje = "Se guardó tu estado '${currentState.categoriaSeleccionada?.nombre}' en el dispositivo."
            )

            // Actualizar UI
            _uiState.update {
                it.copy(
                    categoriaSeleccionada = null,
                    nivelIntensidad = 3f,
                    contexto = "",
                    notaOpcional = "",
                    errorCategoria = null,
                    errorContexto = null,
                    bannerExitoVisible = true
                )
            }
        }

        return true
    }
}