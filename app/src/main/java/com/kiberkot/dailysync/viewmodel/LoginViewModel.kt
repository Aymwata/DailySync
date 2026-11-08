package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.SesionPreferencias
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
    val errorSesion: String? = null
)

class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao(),
    private val sesion: SesionPreferencias = SesionPreferencias(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState(restaurandoSesion = true))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init { restaurarSesion() }

    fun restaurarSesion() {
        _uiState.update { it.copy(restaurandoSesion = true, errorSesion = null) }
        viewModelScope.launch {
            try {
                val usuarioId = withContext(Dispatchers.IO) { sesion.obtenerUsuario() }
                val cuentaValida = usuarioId?.let { dao.obtenerUsuario(it) }?.idUsuario
                if (usuarioId != null && cuentaValida == null) withContext(Dispatchers.IO) { sesion.borrar() }
                // El disco es la autoridad: un SavedStateHandle antiguo no puede revivir un logout.
                savedState["usuarioId"] = cuentaValida
                _uiState.value = LoginUiState(usuarioId = cuentaValida)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(restaurandoSesion = false,
                    errorSesion = "No se pudo recuperar tu sesión. Intenta nuevamente.") }
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
                withContext(Dispatchers.IO) { sesion.guardar(usuarioEncontrado.idUsuario) }
                savedState["usuarioId"] = usuarioEncontrado.idUsuario
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginExitoso = true,
                        usuarioId = usuarioEncontrado.idUsuario,
                        password = "",
                        isPasswordVisible = false
                    )
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo iniciar sesión. Intenta nuevamente.") }
            }
        }
    }

    fun cerrarSesion() {
        if (_uiState.value.isLoading || _uiState.value.restaurandoSesion) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { sesion.borrar() }
                savedState["usuarioId"] = null
                _uiState.value = LoginUiState()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo cerrar sesión. Intenta nuevamente.") }
            }
        }
    }

    fun resetearEstadoLogin() {
        _uiState.update { it.copy(loginExitoso = false, password = "") }
    }
}
