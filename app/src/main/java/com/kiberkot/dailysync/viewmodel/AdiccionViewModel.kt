package com.kiberkot.dailysync.viewmodel

import com.kiberkot.dailysync.utils.NotificationHelper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.CatalogoAdiccionEntity
import com.kiberkot.dailysync.data.local.entities.RegistroAdiccionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AdiccionUiState(
    val catalogoAcciones: List<CatalogoAdiccionEntity> = emptyList(),
    val idRegistroExistente: String? = null,
    val editando: Boolean = false,
    val idAccionSeleccionada: Int? = null,
    val nivelImpulso: Int = 5,
    val desencadenante: String = "",
    val estadoObjetivo: String = "Logrado",
    val fechaHora: String = "",
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class AdiccionViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationHelper = NotificationHelper(application)
    private val dao = AppDatabase.getDatabase(application).dailySyncDao()

    private val _uiState = MutableStateFlow(AdiccionUiState())
    val uiState: StateFlow<AdiccionUiState> = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    init {
        viewModelScope.launch {
            dao.obtenerCatalogoAdiccion().collect { catalogo ->
                _uiState.update { it.copy(catalogoAcciones = catalogo) }
            }
        }
    }

    fun cargar(usuarioId: String, registroId: String? = null) {
        cargaJob?.cancel()
        if (!registroId.isNullOrBlank()) {
            cargaJob = viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val registro = dao.observarRegistroAdiccion(registroId, usuarioId).firstOrNull()
                if (registro != null) {
                    _uiState.update {
                        it.copy(
                            idRegistroExistente = registro.idRegistro,
                            editando = true,
                            nivelImpulso = registro.nivelImpulso,
                            desencadenante = registro.desencadenante,
                            idAccionSeleccionada = registro.idAccionProtectora,
                            estadoObjetivo = registro.estadoObjetivo,
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
                    nivelImpulso = 5,
                    desencadenante = "",
                    idAccionSeleccionada = null,
                    estadoObjetivo = "Logrado",
                    fechaHora = "",
                    isLoading = false,
                    guardadoExitoso = false
                )
            }
        }
    }

    fun cambiarNivelImpulso(valor: Int) {
        _uiState.update { it.copy(nivelImpulso = valor) }
    }

    fun cambiarDesencadenante(valor: String) {
        _uiState.update { it.copy(desencadenante = valor, errorMsg = null) }
    }

    fun seleccionarAccion(id: Int) {
        _uiState.update { it.copy(idAccionSeleccionada = id, errorMsg = null) }
    }

    fun cambiarEstadoObjetivo(valor: String) {
        _uiState.update { it.copy(estadoObjetivo = valor) }
    }

    fun guardarRegistro(idUsuario: String) {
        val state = _uiState.value
        if (state.isLoading || state.guardadoExitoso) return

        if (state.desencadenante.trim().isEmpty()) {
            _uiState.update { it.copy(errorMsg = "Describe el factor desencadenante.") }
            return
        }
        if (state.idAccionSeleccionada == null) {
            _uiState.update { it.copy(errorMsg = "Selecciona una acción protectora.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val fechaHoraActual = state.fechaHora.ifEmpty {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            }

            val registro = RegistroAdiccionEntity(
                idRegistro = state.idRegistroExistente ?: "adic_${UUID.randomUUID()}",
                idUsuario = idUsuario,
                idAccionProtectora = state.idAccionSeleccionada,
                fechaHora = fechaHoraActual,
                nivelImpulso = state.nivelImpulso,
                desencadenante = state.desencadenante.trim(),
                estadoObjetivo = state.estadoObjetivo,
                pendienteSync = true
            )

            // insertarRegistroAdiccion usa OnConflictStrategy.REPLACE, por lo que sirve tanto para crear como para editar
            dao.insertarRegistroAdiccion(registro)
            notificationHelper.mostrarNotificacionCheckIn(
                "DailySync", if (state.editando) "Registro actualizado." else "Nuevo registro de adicciones guardado localmente."
            )
            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }

    fun resetearEstado() {
        _uiState.update {
            AdiccionUiState(catalogoAcciones = it.catalogoAcciones)
        }
    }
}