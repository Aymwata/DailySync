package com.kiberkot.dailysync.viewmodel

import com.kiberkot.dailysync.utils.NotificationHelper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.CatalogoAdiccionEntity
import com.kiberkot.dailysync.data.local.entities.RegistroAdiccionEntity
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import kotlinx.coroutines.CancellationException
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
    val isLoading: Boolean = true,
    val guardando: Boolean = false,
    val cargaFallida: Boolean = false,
    val noDisponible: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class AdiccionViewModel @JvmOverloads constructor(
    application: Application,
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao()
) : AndroidViewModel(application) {
    private val notificationHelper = NotificationHelper(application)
    private val _uiState = MutableStateFlow(AdiccionUiState())
    val uiState: StateFlow<AdiccionUiState> = _uiState.asStateFlow()
    private var cargaJob: Job? = null
    private var claveCarga: Triple<String, String?, Boolean>? = null

    fun cargar(usuarioId: String, registroId: String? = null, soloLectura: Boolean = false) {
        val clave = Triple(usuarioId, registroId, soloLectura)
        if (_uiState.value.guardando || (claveCarga == clave && cargaJob?.isActive == true && !_uiState.value.cargaFallida && !_uiState.value.noDisponible)) return
        cargaJob?.cancel()
        claveCarga = clave
        _uiState.value = AdiccionUiState(editando = registroId != null)
        cargaJob = viewModelScope.launch {
            try {
                check(dao.obtenerUsuario(usuarioId) != null)
                var inicializado = false
                val registroFlow = registroId?.let { dao.observarRegistroAdiccion(it, usuarioId) } ?: flowOf(null)
                combine(dao.obtenerCatalogoAdiccion(), registroFlow) { catalogo, registro -> catalogo to registro }
                    .collect { (catalogo, registro) ->
                        _uiState.update { actual ->
                            val base = if ((!inicializado || soloLectura) && registro != null) actual.copy(
                                idRegistroExistente = registro.idRegistro, nivelImpulso = registro.nivelImpulso, desencadenante = registro.desencadenante,
                                idAccionSeleccionada = registro.idAccionProtectora, estadoObjetivo = registro.estadoObjetivo,
                                fechaHora = registro.fechaHora
                            ) else actual
                            if (registroId != null && registro == null) {
                                AdiccionUiState(catalogoAcciones = catalogo, editando = true, isLoading = false,
                                    noDisponible = true, errorMsg = "No se encontró el registro solicitado.")
                            } else base.copy(catalogoAcciones = catalogo, isLoading = false, cargaFallida = false)
                        }
                        inicializado = true
                    }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, cargaFallida = true,
                    errorMsg = "No se pudieron cargar los datos. Intenta nuevamente.") }
            }
        }
    }

    fun cambiarNivelImpulso(valor: Int) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(nivelImpulso = valor) }
    }

    fun cambiarDesencadenante(valor: String) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(desencadenante = valor, errorMsg = null) }
    }

    fun seleccionarAccion(id: Int) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(idAccionSeleccionada = id, errorMsg = null) }
    }

    fun cambiarEstadoObjetivo(valor: String) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(estadoObjetivo = valor) }
    }

    fun guardarRegistro(usuarioId: String) {
        val state = _uiState.value
        val clave = claveCarga ?: return
        if (usuarioId != clave.first || state.isLoading || state.guardando || state.guardadoExitoso || state.noDisponible || state.cargaFallida) return
        val error = if (state.desencadenante.trim().isEmpty()) "Describe el factor desencadenante."
        else if (state.idAccionSeleccionada !in state.catalogoAcciones.map { it.idAccionProtectora }) "Selecciona una acción del catálogo."
        else if (state.nivelImpulso !in 1..10) "El impulso debe estar entre 1 y 10."
        else if (state.estadoObjetivo !in listOf("Logrado", "En proceso", "Resistido")) "Selecciona un objetivo válido."
        else null
        if (error != null) {
            _uiState.update { it.copy(errorMsg = error) }
            return
        }
        _uiState.update { it.copy(guardando = true, errorMsg = null) }
        viewModelScope.launch {
            try {
                check(dao.obtenerUsuario(usuarioId) != null)
                val id = clave.second
                if (id != null) {
                    check(dao.actualizarAdiccionDelUsuario(id, usuarioId, requireNotNull(state.idAccionSeleccionada), state.nivelImpulso, state.desencadenante.trim(), state.estadoObjetivo) == 1)
                } else {
                    val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date())
                    dao.insertarRegistroAdiccion(RegistroAdiccionEntity(idRegistro = UUID.randomUUID().toString(), idUsuario = usuarioId,
                        idAccionProtectora = requireNotNull(state.idAccionSeleccionada), fechaHora = fecha,
                        nivelImpulso = state.nivelImpulso, desencadenante = state.desencadenante.trim(),
                        estadoObjetivo = state.estadoObjetivo))
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, errorMsg = "No se pudo guardar el registro. Intenta nuevamente.") }
                return@launch
            }
            notificationHelper.mostrarNotificacionCheckIn("DailySync",
                if (state.editando) "Registro actualizado." else "Nuevo registro guardado localmente.")
            _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
        }
    }

    fun resetearEstado() {
        _uiState.update {
            it.copy(
                guardadoExitoso = false,
                guardando = false,
                errorMsg = null
            )
        }
    }
}
