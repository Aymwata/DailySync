package com.kiberkot.dailysync.viewmodel

import com.kiberkot.dailysync.utils.NotificationHelper
import kotlinx.coroutines.delay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import com.kiberkot.dailysync.data.local.entities.RegistroDbtEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
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
    val bannerExitoVisible: Boolean = false,
    val errores: Map<String, String> = emptyMap()
)

class RegistroDbtViewModel @JvmOverloads constructor(
    application: Application,
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao()
) : AndroidViewModel(application) {
    private val notificationHelper = NotificationHelper(application)
    private val _uiState = MutableStateFlow(RegistroDbtUiState())
    val uiState = _uiState.asStateFlow()
    private var observacion: Job? = null
    private var claveCarga: Triple<String, String?, Int?>? = null

    fun cargar(usuarioId: String, registroId: String? = null, habilidadId: Int? = null) {
        val clave = Triple(usuarioId, registroId, habilidadId)
        if (_uiState.value.procesando || (claveCarga == clave && observacion?.isActive == true && _uiState.value.error == null)) return
        claveCarga = clave
        observacion?.cancel()
        _uiState.value = RegistroDbtUiState()
        observacion = viewModelScope.launch {
            try {
                check(dao.obtenerUsuario(usuarioId) != null) { "El usuario no está disponible." }
                val registros = if (registroId == null) flowOf(null) else dao.observarPracticaDbt(registroId, usuarioId)
                var formularioInicializado = false
                combine(dao.obtenerCatalogoDbt(), registros) { catalogo, registro -> catalogo to registro }
                    .collect { (entidades, entidad) ->
                        val catalogo = entidades.map {
                            HabilidadDbt(it.idHabilidad, it.nombreHabilidad,
                                it.moduloDbt, it.contenidoPsicoeducativo)
                        }
                        val registro = entidad?.let {
                            val habilidad = catalogo.find { h -> h.id == it.idHabilidad }
                            RegistroDbt(it.idRegistro, it.idUsuario, it.idHabilidad,
                                habilidad?.nombre ?: "Habilidad DBT", habilidad?.modulo.orEmpty(),
                                it.fechaHora, it.situacionAsociada, it.nivelIntensidad,
                                it.utilidadPercibida, it.pendienteSync)
                        }
                        _uiState.update { actual ->
                            val formulario = if (registroId != null && registro == null) FormularioDbt() else if (formularioInicializado) actual.formulario else {
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

    private fun validarFormulario(
        formulario: FormularioDbt,
        habilidadesDisponibles: Set<Int>
    ): Map<String, String> = buildMap {
        if (formulario.habilidadId == null || formulario.habilidadId !in habilidadesDisponibles) {
            put("habilidad", "Selecciona una habilidad del catálogo.")
        }
        if (formulario.situacion.trim().length !in 3..250) {
            put("situacion", "Escribe entre 3 y 250 caracteres.")
        }
        if (formulario.intensidad !in 1..5) {
            put("intensidad", "La intensidad debe estar entre 1 y 5.")
        }
        if (formulario.utilidad !in 1..5) {
            put("utilidad", "La utilidad debe estar entre 1 y 5.")
        }
    }

    private fun cambiar(transformar: (FormularioDbt) -> FormularioDbt) {
        if (_uiState.value.procesando || _uiState.value.cargando || (claveCarga?.second != null && _uiState.value.registro == null)) return
        _uiState.update {
            val formulario = transformar(it.formulario)
            val errores = if (it.errores.isEmpty()) emptyMap() else {
                validarFormulario(formulario, it.catalogo.map { h -> h.id }.toSet())
            }
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
        if (clave.second != null && actual.registro == null) {
            _uiState.update { it.copy(error = "No se encontró el registro solicitado.") }
            return
        }
        val errores = validarFormulario(actual.formulario, actual.catalogo.map { it.id }.toSet())
        _uiState.update { it.copy(errores = errores, error = null) }
        if (errores.isNotEmpty()) return
        ejecutar {
            val usuarioId = clave.first
            val registroId = clave.second
            val formulario = actual.formulario
            check(dao.obtenerUsuario(usuarioId) != null) { "El usuario no está disponible." }
            val habilidad = formulario.habilidadId?.let { dao.buscarHabilidadDbt(it) }
            require(validarFormulario(formulario, setOfNotNull(habilidad?.idHabilidad)).isEmpty()) {
                "Revisa los datos del formulario."
            }
            val habilidadId = requireNotNull(formulario.habilidadId)
            val id = if (registroId != null) {
                check(dao.actualizarPracticaDbt(registroId, usuarioId, habilidadId,
                    formulario.situacion.trim(), formulario.intensidad, formulario.utilidad) == 1) {
                    "El registro ya no está disponible."
                }
                registroId
            } else {
                val nuevoId = UUID.randomUUID().toString()
                dao.crearPracticaDbt(RegistroDbtEntity(
                    idRegistro = nuevoId,
                    idUsuario = usuarioId,
                    idHabilidad = habilidadId,
                    fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date()),
                    situacionAsociada = formulario.situacion.trim(),
                    nivelIntensidad = formulario.intensidad,
                    utilidadPercibida = formulario.utilidad,
                    pendienteSync = true
                ))
                nuevoId
            }
            notificationHelper.mostrarNotificacionCheckIn(
                "DailySync", if (clave.second != null) "Práctica DBT actualizada." else "Nueva práctica DBT guardada localmente."
            )
            _uiState.update { it.copy(bannerExitoVisible = true) }
            delay(1000L)
            _uiState.update { it.copy(bannerExitoVisible = false) }
            onExito(id)
        }
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
