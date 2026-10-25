package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.CatalogoNeuroEntity
import com.kiberkot.dailysync.data.local.entities.RegistroNeuroEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NeuroUiState(
    val catalogoEstrategias: List<CatalogoNeuroEntity> = emptyList(),
    val idRegistroExistente: String? = null,
    val editando: Boolean = false,
    val idEstrategiaSeleccionada: Int? = null,
    val tipoActividad: String = "",
    val nivelSobrecarga: Int = 3,
    val contexto: String = "",
    val resultadoPercibido: Int = 3,
    val fechaHora: String = "",
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class NeuroViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dailySyncDao()

    private val _uiState = MutableStateFlow(NeuroUiState())
    val uiState: StateFlow<NeuroUiState> = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    init {
        viewModelScope.launch {
            dao.obtenerCatalogoNeuro().collect { catalogo ->
                _uiState.update { it.copy(catalogoEstrategias = catalogo) }
            }
        }
    }

    fun cargar(usuarioId: String, registroId: String? = null) {
        cargaJob?.cancel()
        if (!registroId.isNullOrBlank()) {
            cargaJob = viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val registro = dao.observarRegistroNeuro(registroId, usuarioId).firstOrNull()
                if (registro != null) {
                    _uiState.update {
                        it.copy(
                            idRegistroExistente = registro.idRegistro,
                            editando = true,
                            tipoActividad = registro.tipoActividad,
                            nivelSobrecarga = registro.nivelSobrecarga,
                            idEstrategiaSeleccionada = registro.idEstrategia,
                            contexto = registro.contexto,
                            resultadoPercibido = registro.resultadoPercibido,
                            fechaHora = registro.fechaHora,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMsg = "No se encontró el registro.") }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    idRegistroExistente = null,
                    editando = false,
                    tipoActividad = "",
                    nivelSobrecarga = 3,
                    idEstrategiaSeleccionada = null,
                    contexto = "",
                    resultadoPercibido = 3,
                    fechaHora = "",
                    isLoading = false,
                    guardadoExitoso = false
                )
            }
        }
    }

    fun cambiarTipoActividad(valor: String) {
        _uiState.update { it.copy(tipoActividad = valor, errorMsg = null) }
    }

    fun cambiarNivelSobrecarga(valor: Int) {
        _uiState.update { it.copy(nivelSobrecarga = valor) }
    }

    fun seleccionarEstrategia(id: Int) {
        _uiState.update { it.copy(idEstrategiaSeleccionada = id, errorMsg = null) }
    }

    fun cambiarContexto(valor: String) {
        _uiState.update { it.copy(contexto = valor) }
    }

    fun cambiarResultadoPercibido(valor: Int) {
        _uiState.update { it.copy(resultadoPercibido = valor) }
    }

    fun guardarRegistro(idUsuario: String) {
        val state = _uiState.value

        if (state.tipoActividad.trim().isEmpty()) {
            _uiState.update { it.copy(errorMsg = "Especifica el tipo de actividad o rutina.") }
            return
        }
        if (state.idEstrategiaSeleccionada == null) {
            _uiState.update { it.copy(errorMsg = "Selecciona una estrategia de autorregulación.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val fechaHoraActual = state.fechaHora.ifEmpty {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            }

            val registro = RegistroNeuroEntity(
                idRegistro = state.idRegistroExistente ?: "neuro_${UUID.randomUUID()}",
                idUsuario = idUsuario,
                idEstrategia = state.idEstrategiaSeleccionada,
                fechaHora = fechaHoraActual,
                tipoActividad = state.tipoActividad.trim(),
                nivelSobrecarga = state.nivelSobrecarga,
                contexto = state.contexto.trim(),
                resultadoPercibido = state.resultadoPercibido,
                pendienteSync = true
            )

            // insertarRegistroNeuro usa OnConflictStrategy.REPLACE, por lo que sirve tanto para crear como para editar
            dao.insertarRegistroNeuro(registro)
            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }

    fun resetearEstado() {
        _uiState.update {
            NeuroUiState(catalogoEstrategias = it.catalogoEstrategias)
        }
    }
}