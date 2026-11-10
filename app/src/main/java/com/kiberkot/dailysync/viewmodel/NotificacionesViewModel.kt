package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import com.kiberkot.dailysync.reminders.RecordatoriosDiarios
import com.kiberkot.dailysync.reminders.horaRecordatorioValida
import com.kiberkot.dailysync.utils.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class NotificacionesUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val activo: Boolean = false,
    val hora: String = "20:00",
    val permitidas: Boolean = false,
    val permisosCargados: Boolean = false,
    val error: String? = null
)

class NotificacionesViewModel @JvmOverloads constructor(
    application: Application,
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao(),
    private val recordatorios: RecordatoriosDiarios = RecordatoriosDiarios(application, dao)
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState = _uiState.asStateFlow()
    private val avisos = NotificationHelper(application)
    private var usuarioId: String? = null
    private var observacion: Job? = null
    private var consultaPermisos: Job? = null

    init { actualizarPermisos() }

    fun cargar(usuario: String) {
        if (usuarioId == usuario && observacion?.isActive == true && _uiState.value.error == null) return
        observacion?.cancel()
        usuarioId = usuario
        _uiState.update { NotificacionesUiState(permitidas = it.permitidas, permisosCargados = it.permisosCargados) }
        observacion = viewModelScope.launch {
            try {
                dao.observarUsuario(usuario).collect { cuenta ->
                    checkNotNull(cuenta) { "La cuenta no está disponible." }
                    _uiState.update { it.copy(cargando = false, activo = cuenta.recordatorioActivo,
                        hora = cuenta.horaRecordatorio?.takeIf(::horaRecordatorioValida) ?: "20:00") }
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo cargar la configuración. Reintenta.") }
            }
        }
    }

    fun actualizarPermisos() {
        consultaPermisos?.cancel()
        consultaPermisos = viewModelScope.launch {
            // NotificationManager consulta otro proceso: nunca bloquear la transición de entrada.
            val permitidas = withContext(Dispatchers.IO) {
                avisos.prepararCanal()
                avisos.permitidas()
            }
            _uiState.update { it.copy(permitidas = permitidas, permisosCargados = true) }
        }
    }

    fun cambiarActivo(activo: Boolean) { guardar(activo, _uiState.value.hora) }
    fun cambiarHora(hora: String) { guardar(_uiState.value.activo, hora) }

    fun reintentar() {
        val usuario = usuarioId ?: return
        if (observacion?.isActive == true && !_uiState.value.cargando) guardar(_uiState.value.activo, _uiState.value.hora)
        else cargar(usuario)
    }

    private fun guardar(activo: Boolean, hora: String) {
        val usuario = usuarioId ?: return
        val state = _uiState.value
        if (state.cargando || state.guardando || !horaRecordatorioValida(hora)) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { recordatorios.configurar(usuario, activo, hora) }
                _uiState.update { it.copy(guardando = false, activo = activo, hora = hora) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar el recordatorio. Reintenta.") }
            }
        }
    }
}
