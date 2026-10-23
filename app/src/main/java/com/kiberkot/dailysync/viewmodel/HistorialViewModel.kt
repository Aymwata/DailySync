package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.repository.HistorialRepository
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.model.RegistroHistorial
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    val registros: List<RegistroHistorial> = emptyList(),
    val filtroSeleccionado: AmbitoRegistro? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null,
    val registroAEliminar: RegistroHistorial? = null,
    val eliminando: Boolean = false
) {
    val registrosVisibles: List<RegistroHistorial>
        get() = registros.filter { registro ->
            filtroSeleccionado == null ||
                    registro.ambito == filtroSeleccionado
        }
}

class HistorialViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HistorialRepository(
        AppDatabase.getDatabase(application).dailySyncDao()
    )

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState = _uiState.asStateFlow()

    private var observacion: Job? = null
    private var usuarioActual: String = ""

    fun cargar(usuarioId: String) {
        if (_uiState.value.eliminando) return

        observacion?.cancel()
        usuarioActual = usuarioId
        _uiState.value = HistorialUiState()

        observacion = viewModelScope.launch {
            try {
                repository.observarHistorial(usuarioId)
                    .collect { registros ->
                        _uiState.update {
                            it.copy(
                                registros = registros,
                                cargando = false
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo cargar el historial."
                    )
                }
            }
        }
    }

    fun seleccionarFiltro(ambito: AmbitoRegistro?) {
        _uiState.update {
            it.copy(filtroSeleccionado = ambito)
        }
    }

    fun solicitarEliminacion(registro: RegistroHistorial) {
        if (_uiState.value.eliminando) return

        _uiState.update {
            it.copy(
                registroAEliminar = registro,
                error = null,
                mensaje = null
            )
        }
    }

    fun cancelarEliminacion() {
        if (_uiState.value.eliminando) return

        _uiState.update {
            it.copy(registroAEliminar = null)
        }
    }

    fun confirmarEliminacion() {
        val state = _uiState.value
        val registro = state.registroAEliminar ?: return
        if (state.eliminando) return

        val usuarioId = usuarioActual

        _uiState.update {
            it.copy(eliminando = true, error = null)
        }

        viewModelScope.launch {
            try {
                repository.eliminar(registro, usuarioId)

                _uiState.update {
                    it.copy(
                        eliminando = false,
                        registroAEliminar = null,
                        mensaje = "Registro eliminado."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        eliminando = false,
                        registroAEliminar = null,
                        error = "No se pudo eliminar el registro."
                    )
                }
            }
        }
    }
}