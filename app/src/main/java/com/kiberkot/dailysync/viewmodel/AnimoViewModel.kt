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

// Catálogo sintético base (Lemac DataLab) como respaldo inicial
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
    val bannerExitoVisible: Boolean = false
)

class AnimoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailySyncDao()
    private val notificationHelper = NotificationHelper(application)

    private val _uiState = MutableStateFlow(AnimoUiState())
    val uiState: StateFlow<AnimoUiState> = _uiState.asStateFlow()

    init {
        // 1. Asegurar datos base en Room para respetar ForeignKeys (Offline-First)
        viewModelScope.launch {
            val usuarioExistente = dao.obtenerUsuario("usr_demo_01")
            if (usuarioExistente == null) {
                dao.insertarUsuario(
                    UsuarioPruebaEntity(
                        idUsuario = "usr_demo_01",
                        aliasFicticio = "UsuarioPrueba",
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

        // 2. Combinar reactivamente Catálogo y Registros desde Room (evita condición de carrera)
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

    fun guardarRegistro(): Boolean {
        val currentState = _uiState.value

        // Validación 1: Selección obligatoria de categoría emocional
        val errCategoria = if (currentState.categoriaSeleccionada == null) {
            "Debes seleccionar una categoría de estado de ánimo."
        } else {
            null
        }

        // Validación 2: Reglas de obligatoriedad y longitud para el contexto
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
        val timestampActual = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val nuevoId = UUID.randomUUID().toString()

        val nuevaEntidad = RegistroAnimoEntity(
            idRegistro = nuevoId,
            idUsuario = "usr_demo_01",
            idCategoria = categoriaElegida.id,
            fechaHora = timestampActual,
            nivelIntensidad = currentState.nivelIntensidad.toInt(),
            contexto = contextoLimpio,
            notaOpcional = currentState.notaOpcional.trim().ifBlank { null },
            pendienteSync = true
        )

        viewModelScope.launch {
            // Guardar asíncronamente en Room DB
            dao.insertarRegistroAnimo(nuevaEntidad)

            // Emitir Notificación Nativa
            notificationHelper.mostrarNotificacionCheckIn(
                titulo = "Check-in Registrado",
                mensaje = "Se guardó tu registro en DailySync correctamente."
            )

            // Limpiar formulario y mostrar confirmación
            _uiState.update {
                it.copy(
                    categoriaSeleccionada = null,
                    nivelIntensidad = 3f,
                    contexto = "",
                    notaOpcional = "",
                    errorCategoria = null,
                    errorContexto = null,
                    bannerExitoVisible = true
                )
            }
        }

        return true
    }

    fun buscarPorId(id: String): RegistroAnimo? {
        return _uiState.value.historial.firstOrNull { it.id == id }
    }
}