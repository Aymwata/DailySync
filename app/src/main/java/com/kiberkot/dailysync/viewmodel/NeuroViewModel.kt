package com.kiberkot.dailysync.viewmodel

import com.kiberkot.dailysync.utils.NotificationHelper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.CatalogoNeuroEntity
import com.kiberkot.dailysync.data.local.entities.RegistroNeuroEntity
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import kotlinx.coroutines.CancellationException
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
    val isLoading: Boolean = true,
    val guardando: Boolean = false,
    val cargaFallida: Boolean = false,
    val noDisponible: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class NeuroViewModel @JvmOverloads constructor(
    application: Application,
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao()
) : AndroidViewModel(application) {
    private val notificationHelper = NotificationHelper(application)
    private val _uiState = MutableStateFlow(NeuroUiState())
    val uiState: StateFlow<NeuroUiState> = _uiState.asStateFlow()
    private var cargaJob: Job? = null
    private var claveCarga: Triple<String, String?, Boolean>? = null

    fun cargar(usuarioId: String, registroId: String? = null, soloLectura: Boolean = false) {
        val clave = Triple(usuarioId, registroId, soloLectura)
        if (_uiState.value.guardando || (claveCarga == clave && cargaJob?.isActive == true && !_uiState.value.cargaFallida && !_uiState.value.noDisponible)) return
        cargaJob?.cancel()
        claveCarga = clave
        _uiState.value = NeuroUiState(editando = registroId != null)
        cargaJob = viewModelScope.launch {
            try {
                check(dao.obtenerUsuario(usuarioId) != null)
                var inicializado = false
                val registroFlow = registroId?.let { dao.observarRegistroNeuro(it, usuarioId) } ?: flowOf(null)
                combine(dao.obtenerCatalogoNeuro(), registroFlow) { catalogo, registro -> catalogo to registro }
                    .collect { (catalogo, registro) ->
                        _uiState.update { actual ->
                            val base = if ((!inicializado || soloLectura) && registro != null) actual.copy(
                                idRegistroExistente = registro.idRegistro, tipoActividad = registro.tipoActividad, nivelSobrecarga = registro.nivelSobrecarga,
                                idEstrategiaSeleccionada = registro.idEstrategia, contexto = registro.contexto,
                                resultadoPercibido = registro.resultadoPercibido,
                                fechaHora = registro.fechaHora
                            ) else actual
                            if (registroId != null && registro == null) {
                                NeuroUiState(catalogoEstrategias = catalogo, editando = true, isLoading = false,
                                    noDisponible = true, errorMsg = "No se encontró el registro solicitado.")
                            } else base.copy(catalogoEstrategias = catalogo, isLoading = false, cargaFallida = false)
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

    fun cambiarTipoActividad(valor: String) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(tipoActividad = valor, errorMsg = null) }
    }

    fun cambiarNivelSobrecarga(valor: Int) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(nivelSobrecarga = valor) }
    }

    fun seleccionarEstrategia(id: Int) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(idEstrategiaSeleccionada = id, errorMsg = null) }
    }

    fun cambiarContexto(valor: String) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(contexto = valor) }
    }

    fun cambiarResultadoPercibido(valor: Int) {
        _uiState.update { if (it.isLoading || it.guardando || it.guardadoExitoso || it.cargaFallida || it.noDisponible) it else it.copy(resultadoPercibido = valor) }
    }

    fun guardarRegistro(usuarioId: String) {
        val state = _uiState.value
        val clave = claveCarga ?: return
        if (usuarioId != clave.first || state.isLoading || state.guardando || state.guardadoExitoso || state.noDisponible || state.cargaFallida) return
        val error = if (state.tipoActividad.trim().isEmpty()) "Especifica el tipo de actividad o rutina."
        else if (state.idEstrategiaSeleccionada !in state.catalogoEstrategias.map { it.idEstrategia }) "Selecciona una estrategia del catálogo."
        else if (state.nivelSobrecarga !in 1..5 || state.resultadoPercibido !in 1..5) "Las escalas deben estar entre 1 y 5."
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
                    check(dao.actualizarNeuroDelUsuario(id, usuarioId, requireNotNull(state.idEstrategiaSeleccionada), state.tipoActividad.trim(), state.nivelSobrecarga, state.contexto.trim(), state.resultadoPercibido) == 1)
                } else {
                    val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date())
                    dao.insertarRegistroNeuro(RegistroNeuroEntity(idRegistro = UUID.randomUUID().toString(), idUsuario = usuarioId,
                        idEstrategia = requireNotNull(state.idEstrategiaSeleccionada), fechaHora = fecha,
                        tipoActividad = state.tipoActividad.trim(), nivelSobrecarga = state.nivelSobrecarga,
                        contexto = state.contexto.trim(), resultadoPercibido = state.resultadoPercibido))
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