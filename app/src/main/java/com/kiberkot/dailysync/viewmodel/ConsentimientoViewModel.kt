package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AceptacionConsentimiento
import com.kiberkot.dailysync.data.local.ConsentimientoPreferencias
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val VERSION_CONSENTIMIENTO = 1

data class ConsentimientoUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val aceptacion: AceptacionConsentimiento? = null,
    val error: String? = null
) {
    val aceptado: Boolean get() = aceptacion?.version == VERSION_CONSENTIMIENTO
}

class ConsentimientoViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencias: ConsentimientoPreferencias = ConsentimientoPreferencias(application)
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ConsentimientoUiState())
    val uiState = _uiState.asStateFlow()
    private var usuarioActual: String? = null

    fun cargar(usuarioId: String) {
        if (usuarioActual == usuarioId && _uiState.value.error == null) return
        usuarioActual = usuarioId
        _uiState.value = ConsentimientoUiState()
        viewModelScope.launch {
            try {
                val aceptacion = withContext(Dispatchers.IO) { preferencias.obtener(usuarioId) }
                _uiState.value = ConsentimientoUiState(cargando = false, aceptacion = aceptacion)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.value = ConsentimientoUiState(cargando = false,
                    error = "No se pudo consultar el consentimiento. Intenta nuevamente.")
            }
        }
    }

    fun aceptar() {
        val usuarioId = usuarioActual ?: return
        val state = _uiState.value
        if (state.cargando || state.guardando || state.aceptado || state.error != null) return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                val aceptacion = withContext(Dispatchers.IO) {
                    preferencias.aceptar(usuarioId, VERSION_CONSENTIMIENTO, System.currentTimeMillis())
                }
                _uiState.value = ConsentimientoUiState(cargando = false, aceptacion = aceptacion)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false,
                    error = "No se pudo guardar tu aceptación. Reintenta para continuar.") }
            }
        }
    }
}
