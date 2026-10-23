package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.repository.DbtRepository
import com.kiberkot.dailysync.model.FormularioDbt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.kiberkot.dailysync.model.HabilidadDbt
import com.kiberkot.dailysync.model.RegistroDbt

data class RegistroDbtUiState(
    val catalogo: List<HabilidadDbt> = emptyList(),
    val registro: RegistroDbt? = null,
    val formulario: FormularioDbt = FormularioDbt(),
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val error: String? = null,
    val errores: Map<String, String> = emptyMap()
)

class RegistroDbtViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DbtRepository(AppDatabase.getDatabase(application).dailySyncDao())
    private val _uiState = MutableStateFlow(RegistroDbtUiState())
    val uiState = _uiState.asStateFlow()
    private var observacion: Job? = null
    private var claveCarga: Triple<String, String?, Int?>? = null

    fun cargar(usuarioId: String, registroId: String? = null, habilidadId: Int? = null, forzar: Boolean = false) {
        val clave = Triple(usuarioId, registroId, habilidadId)
        if (_uiState.value.procesando || (!forzar && claveCarga == clave && observacion?.isActive == true)) return
        claveCarga = clave
        observacion?.cancel()
        _uiState.value = RegistroDbtUiState()
        observacion = viewModelScope.launch {
            try {
                repository.prepararCatalogo()
                repository.prepararUsuario(usuarioId)
                val registros = if (registroId == null) flowOf(null) else repository.observarRegistro(registroId, usuarioId)
                var formularioInicializado = false
                combine(repository.observarCatalogo(), registros) { catalogo, registro -> catalogo to registro }
                    .collect { (catalogo, registro) ->
                        _uiState.update { actual ->
                            val formulario = if (formularioInicializado) actual.formulario else {
                                registro?.let { FormularioDbt(it.habilidadId, it.situacion, it.intensidad, it.utilidad) }
                                    ?: FormularioDbt(habilidadId = habilidadId)
                            }
                            actual.copy(catalogo = catalogo, registro = registro, formulario = formulario,
                                cargando = false, error = if (registroId != null && registro == null) {
                                    "No se encontró el registro solicitado."
                                } else actual.error)
                        }
                        formularioInicializado = true
                    }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No se pudieron cargar los datos DBT. Intenta nuevamente.") }
            }
        }
    }

    fun reintentar() { claveCarga?.let { cargar(it.first, it.second, it.third, forzar = true) } }

    private fun cambiar(transformar: (FormularioDbt) -> FormularioDbt) {
        if (_uiState.value.procesando) return
        _uiState.update {
            val formulario = transformar(it.formulario)
            val errores = if (it.errores.isEmpty()) emptyMap() else formulario.errores(it.catalogo.map { h -> h.id }.toSet())
            it.copy(formulario = formulario, errores = errores, error = null)
        }
    }

    fun seleccionarHabilidad(id: Int) = cambiar { it.copy(habilidadId = id) }
    fun cambiarSituacion(valor: String) = cambiar { it.copy(situacion = valor) }
    fun cambiarIntensidad(valor: Int) = cambiar { it.copy(intensidad = valor) }
    fun cambiarUtilidad(valor: Int) = cambiar { it.copy(utilidad = valor) }

    fun guardar(onExito: (String) -> Unit) {
        val clave = claveCarga ?: return
        val actual = _uiState.value
        if (actual.cargando || actual.procesando) return
        val errores = actual.formulario.errores(actual.catalogo.map { it.id }.toSet())
        _uiState.update { it.copy(errores = errores, error = null) }
        if (errores.isNotEmpty()) return
        ejecutar { onExito(repository.guardar(clave.first, clave.second, actual.formulario)) }
    }

    fun eliminar(onExito: () -> Unit) {
        val clave = claveCarga ?: return
        val registro = _uiState.value.registro ?: return
        if (_uiState.value.procesando) return
        ejecutar { repository.eliminar(clave.first, registro.id); onExito() }
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        _uiState.update { it.copy(procesando = true, error = null) }
        viewModelScope.launch {
            try {
                accion()
                _uiState.update { it.copy(procesando = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(procesando = false, error = "No se pudo guardar el cambio. Intenta nuevamente.") }
            }
        }
    }
}
