package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
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
    val loginExitoso: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailySyncDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
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
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loginExitoso = true
                )
            }
        }
    }

    fun resetearEstadoLogin() {
        _uiState.update { it.copy(loginExitoso = false) }
    }
}