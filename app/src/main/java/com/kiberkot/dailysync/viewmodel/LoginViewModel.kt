package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.UsuarioPruebaEntity
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

    fun ejecutarLogin(): Boolean {
        val state = _uiState.value
        var esValido = true
        var errUsuario: String? = null
        var errPassword: String? = null

        if (state.usuarioOrEmail.trim().length < 3) {
            errUsuario = "El usuario debe tener al menos 3 caracteres."
            esValido = false
        }

        if (state.password.trim().length < 4) {
            errPassword = "La contraseña debe tener al menos 4 caracteres."
            esValido = false
        }

        if (!esValido) {
            _uiState.update {
                it.copy(
                    errorUsuario = errUsuario,
                    errorPassword = errPassword
                )
            }
            return false
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val idBuscado = state.usuarioOrEmail.trim()
            val usuarioExistente = dao.obtenerUsuario(idBuscado)

            // Si el usuario no existe en Room, creamos la entidad con las propiedades correctas
            if (usuarioExistente == null) {
                val nuevoUsuario = UsuarioPruebaEntity(
                    idUsuario = idBuscado,
                    aliasFicticio = idBuscado,
                    tokenSesion = "token_demo_local",
                    recordatorioActivo = true,
                    horaRecordatorio = "20:00"
                )
                dao.insertarUsuario(nuevoUsuario)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loginExitoso = true
                )
            }
        }

        return true
    }

    fun resetearEstadoLogin() {
        _uiState.update { it.copy(loginExitoso = false) }
    }
}