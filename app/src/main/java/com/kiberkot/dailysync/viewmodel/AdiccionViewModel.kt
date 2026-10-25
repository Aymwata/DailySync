package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.CatalogoAdiccionEntity
import com.kiberkot.dailysync.data.local.entities.RegistroAdiccionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AdiccionUiState(
    val catalogoAcciones: List<CatalogoAdiccionEntity> = emptyList(),
    val idAccionSeleccionada: Int? = null,
    val nivelImpulso: Int = 5,
    val desencadenante: String = "",
    val estadoObjetivo: String = "Logrado",
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class AdiccionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dailySyncDao()

    private val _uiState = MutableStateFlow(AdiccionUiState())
    val uiState: StateFlow<AdiccionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.obtenerCatalogoAdiccion().collect { catalogo ->
                _uiState.update { it.copy(catalogoAcciones = catalogo) }
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
            val fechaHoraActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val nuevoRegistro = RegistroAdiccionEntity(
                idRegistro = "adic_${UUID.randomUUID()}",
                idUsuario = idUsuario,
                idAccionProtectora = state.idAccionSeleccionada,
                fechaHora = fechaHoraActual,
                nivelImpulso = state.nivelImpulso,
                desencadenante = state.desencadenante.trim(),
                estadoObjetivo = state.estadoObjetivo,
                pendienteSync = true
            )

            dao.insertarRegistroAdiccion(nuevoRegistro)
            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }

    fun resetearEstado() {
        _uiState.update { AdiccionUiState(catalogoAcciones = it.catalogoAcciones) }
    }
}