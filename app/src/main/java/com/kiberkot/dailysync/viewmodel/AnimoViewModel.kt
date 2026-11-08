package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.RegistroAnimoEntity
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.utils.NotificationHelper
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val CATALOGO_ANIMO_DEFAULT = listOf(
    CategoriaAnimo(1, "Tranquilo / Calmo"),
    CategoriaAnimo(2, "Motivado / Con energía"),
    CategoriaAnimo(3, "Ansioso / Inquieto"),
    CategoriaAnimo(4, "Abrumado / Estresado"),
    CategoriaAnimo(5, "Desanimado / Triste")
)

data class AnimoUiState(
    val categorias: List<CategoriaAnimo> = CATALOGO_ANIMO_DEFAULT,
    val historial: List<RegistroAnimo> = emptyList(),
    val categoriaSeleccionada: CategoriaAnimo? = null,
    val nivelIntensidad: Float = 3f,
    val contexto: String = "",
    val notaOpcional: String = "",
    val errorCategoria: String? = null,
    val errorContexto: String? = null,
    val bannerExitoVisible: Boolean = false,
    val idRegistroExistente: String? = null,
    val editando: Boolean = false,
    val fechaHoraOriginal: String = "",
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val cargaFallida: Boolean = false,
    val noDisponible: Boolean = false
)

class AnimoViewModel @JvmOverloads constructor(
    application: Application,
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao()
) : AndroidViewModel(application) {
    private val notificationHelper = NotificationHelper(application)
    private val _uiState = MutableStateFlow(AnimoUiState())
    val uiState: StateFlow<AnimoUiState> = _uiState.asStateFlow()
    private var cargaJob: Job? = null
    private var claveCarga: Pair<String, String?>? = null

    fun cargar(usuarioId: String, registroId: String? = null) {
        val clave = usuarioId to registroId
        if (_uiState.value.guardando || (claveCarga == clave && cargaJob?.isActive == true && !_uiState.value.cargaFallida && !_uiState.value.noDisponible)) return
        cargaJob?.cancel()
        claveCarga = clave
        _uiState.value = AnimoUiState(editando = registroId != null)
        cargaJob = viewModelScope.launch {
            try {
                check(dao.obtenerUsuario(usuarioId) != null)
                var inicializado = false
                val registro = registroId?.let { dao.observarRegistroAnimo(it, usuarioId) } ?: flowOf(null)
                combine(dao.obtenerCatalogoAnimo(), registro) { categorias, entidad ->
                    categorias.map { CategoriaAnimo(it.idCategoria, it.nombreCategoria) } to entidad
                }.collect { (categorias, entidad) ->
                    val modelo = entidad?.let {
                        RegistroAnimo(it.idRegistro, it.idUsuario, it.idCategoria,
                            categorias.find { c -> c.id == it.idCategoria }?.nombre ?: "Estado de ánimo",
                            it.fechaHora, it.nivelIntensidad, it.contexto, it.notaOpcional, it.pendienteSync)
                    }
                    _uiState.update { actual ->
                        val base = if (!inicializado && entidad != null) actual.copy(
                            idRegistroExistente = entidad.idRegistro,
                            categoriaSeleccionada = categorias.find { it.id == entidad.idCategoria },
                            nivelIntensidad = entidad.nivelIntensidad.toFloat(), contexto = entidad.contexto,
                            notaOpcional = entidad.notaOpcional.orEmpty(), fechaHoraOriginal = entidad.fechaHora
                        ) else actual
                        base.copy(categorias = categorias, historial = listOfNotNull(modelo), cargando = false,
                            noDisponible = registroId != null && entidad == null, cargaFallida = false,
                            error = if (registroId != null && entidad == null) "No se encontró el registro solicitado." else actual.error)
                    }
                    inicializado = true
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, cargaFallida = true,
                    error = "No se pudieron cargar los datos. Intenta nuevamente.") }
            }
        }
    }

    private fun cambiar(transformar: (AnimoUiState) -> AnimoUiState) {
        if (_uiState.value.guardando || _uiState.value.cargando || _uiState.value.noDisponible || _uiState.value.cargaFallida) return
        _uiState.update(transformar)
    }
    fun seleccionarCategoria(categoria: CategoriaAnimo) = cambiar { it.copy(categoriaSeleccionada = categoria, errorCategoria = null) }
    fun cambiarIntensidad(valor: Float) = cambiar { it.copy(nivelIntensidad = valor) }
    fun cambiarContexto(valor: String) = cambiar { it.copy(contexto = valor, errorContexto = null) }
    fun cambiarNota(valor: String) = cambiar { it.copy(notaOpcional = valor) }

    fun guardarRegistro(onExito: () -> Unit): Boolean {
        val actual = _uiState.value
        val clave = claveCarga ?: return false
        if (actual.cargando || actual.guardando || actual.noDisponible || actual.cargaFallida) return false
        val errorCategoria = if (actual.categoriaSeleccionada?.id !in actual.categorias.map { it.id }) {
            "Debes seleccionar una categoría de estado de ánimo."
        } else null
        val contexto = actual.contexto.trim()
        val errorContexto = if (contexto.length !in 3..60) "Escribe entre 3 y 60 caracteres para el contexto." else null
        _uiState.update { it.copy(errorCategoria = errorCategoria, errorContexto = errorContexto, error = null) }
        if (errorCategoria != null || errorContexto != null) return false
        if (!actual.nivelIntensidad.isFinite() || actual.nivelIntensidad !in 1f..5f) {
            _uiState.update { it.copy(error = "La intensidad debe estar entre 1 y 5.") }
            return false
        }
        // Bloquear antes de iniciar la escritura evita que dos pulsaciones creen dos registros.
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                val categoria = requireNotNull(actual.categoriaSeleccionada)
                check(dao.obtenerUsuario(clave.first) != null)
                if (clave.second != null) {
                    check(dao.actualizarAnimoDelUsuario(clave.second!!, clave.first, categoria.id,
                        contexto, actual.notaOpcional.trim().ifBlank { null }, actual.nivelIntensidad.toInt()) == 1)
                } else {
                    dao.insertarRegistroAnimo(RegistroAnimoEntity(UUID.randomUUID().toString(), clave.first,
                        categoria.id, SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date()),
                        actual.nivelIntensidad.toInt(), contexto, actual.notaOpcional.trim().ifBlank { null }))
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar el registro. Intenta nuevamente.") }
                return@launch
            }
            notificationHelper.mostrarNotificacionCheckIn("DailySync",
                if (actual.editando) "Check-in actualizado." else "Nuevo check-in guardado localmente.")
            _uiState.update { it.copy(bannerExitoVisible = true) }
            delay(1000L)
            onExito()
        }
        return true
    }
}
