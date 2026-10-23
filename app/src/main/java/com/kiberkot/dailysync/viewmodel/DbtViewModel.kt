package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.repository.DbtRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kiberkot.dailysync.model.HabilidadDbt

data class DbtUiState(
    val catalogo: List<HabilidadDbt> = emptyList(),
    val moduloSeleccionado: String? = null,
    val cargando: Boolean = true,
    val error: String? = null
) {
    val modulosDisponibles: List<String>
        get() = catalogo
            .map { it.modulo }
            .distinct()
            .sorted()

    val habilidadesVisibles: List<HabilidadDbt>
        get() = catalogo.filter { habilidad ->
            moduloSeleccionado == null ||
                    habilidad.modulo == moduloSeleccionado
        }
}

class DbtViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = DbtRepository(
        AppDatabase.getDatabase(application).dailySyncDao()
    )

    private val _uiState = MutableStateFlow(DbtUiState())
    val uiState = _uiState.asStateFlow()

    private var observacionCatalogo: Job? = null

    init {
        cargarCatalogo()
    }

    fun cargarCatalogo() {
        observacionCatalogo?.cancel()

        _uiState.update {
            it.copy(
                cargando = true,
                error = null
            )
        }

        observacionCatalogo = viewModelScope.launch {
            try {
                repository.prepararCatalogo()
                repository.observarCatalogo()
                    .collect { habilidades ->
                        _uiState.update {
                            it.copy(
                                catalogo = habilidades,
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
                        error = "No se pudo cargar el catálogo DBT."
                    )
                }
            }
        }
    }

    fun seleccionarModulo(modulo: String?) {
        _uiState.update {
            it.copy(moduloSeleccionado = modulo)
        }
    }
}
