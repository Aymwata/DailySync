package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuarioOrEmail: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorUsuario: String? = null,
    val errorPassword: String? = null,
    val isLoading: Boolean = false,
    val loginExitoso: Boolean = false,
    val usuarioId: String? = null,
    val error: String? = null,
    val restaurandoSesion: Boolean = false,
    val errorSesion: String? = null,
    val avisoSesion: String? = null
)

class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao(),
    private val sesion: SesionPreferencias = SesionPreferencias(application),
    private val reloj: RelojSesion = RelojSesionSistema(application),
    private val inactividadMs: Long = INACTIVIDAD_SESION_MS
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState(restaurandoSesion = true))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var ultimaActividad: InstanteActividad? = null
    private var ultimaPersistencia = 0L
    private var vigilancia: Job? = null
    private var revisionOperacion = 0
    private var cierrePendiente = false
    private var cierre: Job? = null
    private var avisoCierre: String? = null

    init { require(inactividadMs > 0); restaurarSesion() }

    fun restaurarSesion() {
        if (cierrePendiente) { completarCierre(); return }
        val revision = ++revisionOperacion
        vigilancia?.cancel()
        ultimaActividad = null
        _uiState.value = LoginUiState(restaurandoSesion = true)
        viewModelScope.launch {
            try {
                val guardada = withContext(Dispatchers.IO) { sesion.obtenerSesion() }
                if (revision != revisionOperacion) return@launch
                if (guardada != null && tiempoRestanteSesion(guardada.actividad, reloj.ahora(), inactividadMs) == 0L) {
                    bloquearSesion(porInactividad = true)
                    return@launch
                }
                val cuentaValida = guardada?.let { dao.obtenerUsuario(it.usuarioId) }?.idUsuario
                if (guardada != null && cuentaValida == null) withContext(Dispatchers.IO) { sesion.borrar() }
                if (revision != revisionOperacion) return@launch
                if (cuentaValida != null && tiempoRestanteSesion(guardada?.actividad, reloj.ahora(), inactividadMs) == 0L) {
                    bloquearSesion(porInactividad = true)
                    return@launch
                }
                savedState["usuarioId"] = cuentaValida
                ultimaActividad = guardada?.actividad.takeIf { cuentaValida != null }
                ultimaPersistencia = ultimaActividad?.transcurridoMs ?: 0
                _uiState.value = LoginUiState(usuarioId = cuentaValida)
                if (cuentaValida != null) iniciarVigilancia()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (revision == revisionOperacion) _uiState.value = LoginUiState(
                    errorSesion = "No se pudo recuperar tu sesión. Intenta nuevamente.")
            }
        }
    }

    /** Comprueba sin renovar: se usa antes de mostrar la app al volver del segundo plano. */
    fun verificarSesion(): Boolean {
        val state = _uiState.value
        if (state.usuarioId == null) return !state.restaurandoSesion
        if (tiempoRestanteSesion(ultimaActividad, reloj.ahora(), inactividadMs) == 0L) {
            bloquearSesion(porInactividad = true)
            return false
        }
        return true
    }

    /** Devuelve false si el evento llegó con la sesión vencida; nunca la revive. */
    fun registrarInteraccion(): Boolean {
        if (!verificarSesion()) return false
        val usuario = _uiState.value.usuarioId ?: return true
        val ahora = reloj.ahora()
        ultimaActividad = ahora
        // El movimiento del dedo no debe generar escrituras a 60 Hz.
        if (ahora.transcurridoMs - ultimaPersistencia >= 1000) {
            sesion.actualizarActividad(usuario, ahora)
            ultimaPersistencia = ahora.transcurridoMs
        }
        return true
    }

    fun guardarActividadActual() {
        if (!verificarSesion()) return
        val usuario = _uiState.value.usuarioId ?: return
        ultimaActividad?.let { sesion.actualizarActividad(usuario, it) }
    }

    private fun iniciarVigilancia() {
        vigilancia?.cancel()
        vigilancia = viewModelScope.launch {
            while (_uiState.value.usuarioId != null) {
                val restante = tiempoRestanteSesion(ultimaActividad, reloj.ahora(), inactividadMs)
                if (restante == 0L) { bloquearSesion(porInactividad = true); break }
                delay(restante)
            }
        }
    }

    private fun bloquearSesion(porInactividad: Boolean) {
        if (cierrePendiente) return
        ++revisionOperacion
        vigilancia?.cancel()
        ultimaActividad = null
        savedState["usuarioId"] = null
        avisoCierre = if (porInactividad) "Tu sesión se cerró por inactividad. Inicia sesión nuevamente." else null
        cierrePendiente = true
        // Retira las pantallas privadas antes de esperar la escritura en disco.
        _uiState.value = LoginUiState(restaurandoSesion = true, isLoading = true, avisoSesion = avisoCierre)
        runCatching { sesion.revocar() }
        completarCierre()
    }

    private fun completarCierre() {
        if (cierre?.isActive == true) return
        _uiState.value = LoginUiState(restaurandoSesion = true, isLoading = true, avisoSesion = avisoCierre)
        cierre = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { sesion.borrar() }
                cierrePendiente = false
                _uiState.value = LoginUiState(avisoSesion = avisoCierre)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                // Un error al cerrar no vuelve a mostrar contenido privado. Reintentar sólo repite el borrado.
                _uiState.value = LoginUiState(errorSesion = "No se pudo finalizar el cierre de sesión. Reintenta.",
                    avisoSesion = avisoCierre)
            }
        }
    }

    fun cambiarUsuario(valor: String) {
        _uiState.update {
            it.copy(
                usuarioOrEmail = valor,
                errorUsuario = if (valor.trim().length >= 3) null else it.errorUsuario
            )
        }
    }

    fun cambiarPassword(valor: String) {
        _uiState.update {
            it.copy(
                password = valor,
                errorPassword = if (valor.length >= 4) null else it.errorPassword
            )
        }
    }

    fun conmutarVisibilidadPassword() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun ejecutarLogin() {
        val state = _uiState.value
        if (state.isLoading || state.restaurandoSesion || state.errorSesion != null || state.usuarioId != null) return
        var esValido = true
        var errUsuario: String? = null
        var errPassword: String? = null

        if (state.usuarioOrEmail.trim().length < 3) {
            errUsuario = "Ingresa tu usuario o correo."
            esValido = false
        }

        if (state.password.trim().length < 4) {
            errPassword = "Ingresa tu contraseña."
            esValido = false
        }

        if (!esValido) {
            _uiState.update {
                it.copy(
                    errorUsuario = errUsuario,
                    errorPassword = errPassword
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, errorUsuario = null, errorPassword = null) }
        val revision = ++revisionOperacion

        viewModelScope.launch {
            try {
                val busqueda = state.usuarioOrEmail.trim()
                val passIngresada = state.password.trim()

                // Consultar si el usuario existe por ID o Correo
                val usuarioEncontrado = dao.obtenerUsuarioPorIdOCorreo(busqueda)

                if (usuarioEncontrado == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorUsuario = "La cuenta no existe. Por favor regístrate."
                        )
                    }
                    return@launch
                }

                // Validar coincidencia exacta de contraseña
                if (usuarioEncontrado.password != passIngresada) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorPassword = "La contraseña es incorrecta."
                        )
                    }
                    return@launch
                }

                // Autenticación correcta
                val actividad = reloj.ahora()
                withContext(Dispatchers.IO) { sesion.guardar(usuarioEncontrado.idUsuario, actividad) }
                if (revision != revisionOperacion) return@launch
                ultimaActividad = actividad
                ultimaPersistencia = actividad.transcurridoMs
                savedState["usuarioId"] = usuarioEncontrado.idUsuario
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginExitoso = true,
                        usuarioId = usuarioEncontrado.idUsuario,
                        password = "",
                        isPasswordVisible = false,
                        avisoSesion = null
                    )
                }
                iniciarVigilancia()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo iniciar sesión. Intenta nuevamente.") }
            }
        }
    }

    fun cerrarSesion() {
        if (_uiState.value.isLoading || _uiState.value.restaurandoSesion) return
        bloquearSesion(porInactividad = false)
    }

    fun resetearEstadoLogin() {
        _uiState.update { it.copy(loginExitoso = false, password = "") }
    }
}
