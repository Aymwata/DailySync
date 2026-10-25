package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.CatalogoAnimoEntity
import com.kiberkot.dailysync.data.local.entities.RegistroAnimoEntity
import com.kiberkot.dailysync.data.local.entities.UsuarioPruebaEntity
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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
    val fechaHoraOriginal: String = ""
)

class AnimoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailySyncDao()
    private val notificationHelper = NotificationHelper(application)

    private val _uiState = MutableStateFlow(AnimoUiState())
    val uiState: StateFlow<AnimoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuarioExistente = dao.obtenerUsuario("usr_demo_01")
            if (usuarioExistente == null || usuarioExistente.correo.isBlank() || usuarioExistente.password.isBlank()) {
                dao.insertarUsuario(
                    UsuarioPruebaEntity(
                        idUsuario = "usr_demo_01",
                        aliasFicticio = "UsuarioPrueba",
                        correo = "demo@dailysync.app",
                        password = "1234",
                        tokenSesion = "token_ficticio_123",
                        recordatorioActivo = true,
                        horaRecordatorio = "20:00"
                    )
                )
                dao.insertarCatalogoAnimo(
                    CATALOGO_ANIMO_DEFAULT.map {
                        CatalogoAnimoEntity(idCategoria = it.id, nombreCategoria = it.nombre)
                    }
                )
            }
        }

        viewModelScope.launch {
            combine(
                dao.obtenerCatalogoAnimo(),
                dao.obtenerRegistrosAnimo()
            ) { catalogoEntities, registroEntities ->
                val categoriasActuales = if (catalogoEntities.isNotEmpty()) {
                    catalogoEntities.map { entity ->
                        CategoriaAnimo(id = entity.idCategoria, nombre = entity.nombreCategoria)
                    }
                } else {
                    CATALOGO_ANIMO_DEFAULT
                }

                val catalogoMap = categoriasActuales.associateBy { it.id }

                val registrosMapeados = registroEntities.map { entity ->
                    RegistroAnimo(
                        id = entity.idRegistro,
                        usuarioId = entity.idUsuario,
                        categoriaId = entity.idCategoria,
                        categoriaNombre = catalogoMap[entity.idCategoria]?.nombre
                            ?: "Ánimo #${entity.idCategoria}",
                        fechaHora = entity.fechaHora,
                        nivelIntensidad = entity.nivelIntensidad,
                        contexto = entity.contexto,
                        notaOpcional = entity.notaOpcional,
                        pendienteSync = entity.pendienteSync
                    )
                }

                Pair(categoriasActuales, registrosMapeados)
            }.collect { (categorias, registros) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        categorias = categorias,
                        historial = registros
                    )
                }
            }
        }
    }

    fun cargarParaEditar(registroId: String) {
        viewModelScope.launch {
            val registroLocal = _uiState.value.historial.find { it.id == registroId }
            if (registroLocal != null) {
                val cat = _uiState.value.categorias.find { it.id == registroLocal.categoriaId }
                    ?: CategoriaAnimo(registroLocal.categoriaId, registroLocal.categoriaNombre)

                _uiState.update {
                    it.copy(
                        idRegistroExistente = registroLocal.id,
                        editando = true,
                        categoriaSeleccionada = cat,
                        nivelIntensidad = registroLocal.nivelIntensidad.toFloat(),
                        contexto = registroLocal.contexto,
                        notaOpcional = registroLocal.notaOpcional ?: "",
                        fechaHoraOriginal = registroLocal.fechaHora,
                        errorCategoria = null,
                        errorContexto = null
                    )
                }
            } else {
                val entities = dao.obtenerRegistrosAnimo().firstOrNull() ?: emptyList()
                val entity = entities.find { it.idRegistro == registroId }
                if (entity != null) {
                    val catEntities = dao.obtenerCatalogoAnimo().firstOrNull() ?: emptyList()
                    val catName = catEntities.find { it.idCategoria == entity.idCategoria }?.nombreCategoria ?: "Ánimo"
                    val cat = CategoriaAnimo(entity.idCategoria, catName)

                    _uiState.update {
                        it.copy(
                            idRegistroExistente = entity.idRegistro,
                            editando = true,
                            categoriaSeleccionada = cat,
                            nivelIntensidad = entity.nivelIntensidad.toFloat(),
                            contexto = entity.contexto,
                            notaOpcional = entity.notaOpcional ?: "",
                            fechaHoraOriginal = entity.fechaHora,
                            errorCategoria = null,
                            errorContexto = null
                        )
                    }
                }
            }
        }
    }

    fun seleccionarCategoria(categoria: CategoriaAnimo) {
        _uiState.update {
            it.copy(
                categoriaSeleccionada = categoria,
                errorCategoria = null,
                bannerExitoVisible = false
            )
        }
    }

    fun cambiarIntensidad(intensidad: Float) {
        _uiState.update {
            it.copy(
                nivelIntensidad = intensidad,
                bannerExitoVisible = false
            )
        }
    }

    fun cambiarContexto(contexto: String) {
        _uiState.update {
            it.copy(
                contexto = contexto,
                errorContexto = if (contexto.trim().length in 3..60) null else it.errorContexto,
                bannerExitoVisible = false
            )
        }
    }

    fun cambiarNota(nota: String) {
        _uiState.update {
            it.copy(
                notaOpcional = nota,
                bannerExitoVisible = false
            )
        }
    }

    fun ocultarBanner() {
        _uiState.update { it.copy(bannerExitoVisible = false) }
    }

    fun guardarRegistro(onExito: () -> Unit): Boolean {
        val currentState = _uiState.value

        val errCategoria = if (currentState.categoriaSeleccionada == null) {
            "Debes seleccionar una categoría de estado de ánimo."
        } else null

        val contextoLimpio = currentState.contexto.trim()
        val errContexto = when {
            contextoLimpio.isBlank() -> "El contexto o entorno es obligatorio."
            contextoLimpio.length < 3 -> "Usa al menos 3 caracteres para describir el contexto."
            contextoLimpio.length > 60 -> "Usa máximo 60 caracteres para el contexto."
            else -> null
        }

        if (errCategoria != null || errContexto != null) {
            _uiState.update {
                it.copy(
                    errorCategoria = errCategoria,
                    errorContexto = errContexto,
                    bannerExitoVisible = false
                )
            }
            return false
        }

        val categoriaElegida = currentState.categoriaSeleccionada!!
        val timestampActual = currentState.fechaHoraOriginal.ifEmpty {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        }
        val idFinal = currentState.idRegistroExistente ?: UUID.randomUUID().toString()

        val entidad = RegistroAnimoEntity(
            idRegistro = idFinal,
            idUsuario = "usr_demo_01",
            idCategoria = categoriaElegida.id,
            fechaHora = timestampActual,
            nivelIntensidad = currentState.nivelIntensidad.toInt(),
            contexto = contextoLimpio,
            notaOpcional = currentState.notaOpcional.trim().ifBlank { null },
            pendienteSync = true
        )

        viewModelScope.launch {
            if (currentState.editando) {
                dao.actualizarRegistroAnimo(entidad)
            } else {
                dao.insertarRegistroAnimo(entidad)
            }

            notificationHelper.mostrarNotificacionCheckIn(
                titulo = "DailySync",
                mensaje = if (currentState.editando) "Check-in actualizado." else "Nuevo check-in guardado localmente."
            )

            _uiState.update { it.copy(bannerExitoVisible = true) }
            delay(1000L)

            _uiState.update {
                it.copy(
                    categoriaSeleccionada = null,
                    nivelIntensidad = 3f,
                    contexto = "",
                    notaOpcional = "",
                    errorCategoria = null,
                    errorContexto = null,
                    bannerExitoVisible = false,
                    idRegistroExistente = null,
                    editando = false,
                    fechaHoraOriginal = ""
                )
            }

            onExito()
        }

        return true
    }

    fun buscarPorId(id: String): RegistroAnimo? {
        return _uiState.value.historial.find { it.id == id }
    }

    fun eliminarRegistro(id: String, onEliminado: () -> Unit) {
        viewModelScope.launch {
            dao.eliminarRegistroAnimoPorId(id)
            onEliminado()
        }
    }
}