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

data class RegisterUiState(
    val idUsuario: String = "",
    val aliasFicticio: String = "",
    val correo: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorId: String? = null,
    val errorAlias: String? = null,
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dailySyncDao()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun cambiarIdUsuario(valor: String) {
        _uiState.update {
            it.copy(idUsuario = valor, errorId = if (valor.trim().length >= 3) null else it.errorId)
        }
    }

    fun cambiarAlias(valor: String) {
        _uiState.update {
            it.copy(aliasFicticio = valor, errorAlias = if (valor.trim().length >= 2) null else it.errorAlias)
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update {
            val esValido = valor.contains("@") && valor.contains(".")
            it.copy(correo = valor, errorCorreo = if (esValido) null else it.errorCorreo)
        }
    }

    fun cambiarPassword(valor: String) {
        _uiState.update {
            it.copy(password = valor, errorPassword = if (valor.length >= 4) null else it.errorPassword)
        }
    }

    fun conmutarVisibilidadPassword() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun registrarUsuario() {
        val state = _uiState.value
        var esValido = true
        var errId: String? = null
        var errAlias: String? = null
        var errCorreo: String? = null
        var errPassword: String? = null

        if (state.idUsuario.trim().length < 3) {
            errId = "El ID debe tener al menos 3 caracteres."
            esValido = false
        }
        if (state.aliasFicticio.trim().length < 2) {
            errAlias = "El alias debe tener al menos 2 caracteres."
            esValido = false
        }
        if (!state.correo.contains("@") || !state.correo.contains(".")) {
            errCorreo = "Ingresa un correo electrónico válido."
            esValido = false
        }
        if (state.password.trim().length < 4) {
            errPassword = "La contraseña debe tener al menos 4 caracteres."
            esValido = false
        }

        if (!esValido) {
            _uiState.update {
                it.copy(
                    errorId = errId,
                    errorAlias = errAlias,
                    errorCorreo = errCorreo,
                    errorPassword = errPassword
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val idBuscado = state.idUsuario.trim()
            val correoBuscado = state.correo.trim()

            // Verificar si el ID o Correo ya existen en Room
            val usuarioExistente = dao.obtenerUsuarioPorIdOCorreo(idBuscado)
                ?: dao.obtenerUsuarioPorIdOCorreo(correoBuscado)

            if (usuarioExistente != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorId = if (usuarioExistente.idUsuario == idBuscado) "El ID ya se encuentra registrado." else null,
                        errorCorreo = if (usuarioExistente.correo == correoBuscado) "El correo ya está registrado." else null
                    )
                }
                return@launch
            }

            // Inserción de la nueva cuenta en la base de datos local
            val nuevoUsuario = UsuarioPruebaEntity(
                idUsuario = idBuscado,
                aliasFicticio = state.aliasFicticio.trim(),
                correo = correoBuscado,
                password = state.password.trim(),
                tokenSesion = "token_${System.currentTimeMillis()}",
                recordatorioActivo = true,
                horaRecordatorio = "20:00"
            )
            dao.insertarUsuario(nuevoUsuario)

            _uiState.update {
                it.copy(isLoading = false, registroExitoso = true)
            }
        }
    }

    fun resetearEstado() {
        _uiState.update { RegisterUiState() }
    }
}