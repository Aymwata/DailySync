package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.distinctUntilChanged
import com.kiberkot.dailysync.utils.leerFechaRegistro
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ModuloHome(
    val titulo: String
) {
    ANIMO("Estado de Ánimo"),
    DBT("Habilidades DBT"),
    NEURODESARROLLO("Neurodesarrollo"),
    ADICCIONES("Adicciones")
}

enum class SeccionHome {
    INICIO,
    HISTORIAL,
    CONTENIDOS,
    AJUSTES
}

data class ActividadDiaUi(
    val etiqueta: String,
    val cantidad: Int,
    val esHoy: Boolean = false
)

data class HomeUiState(
    val fecha: String = "",
    val saludo: String = "",
    val iniciales: String = "US",
    val actividadSemanal: List<ActividadDiaUi> = emptyList(),
    val error: String? = null
)

class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val dao: DailySyncDao = AppDatabase.getDatabase(application).dailySyncDao()
) : AndroidViewModel(application) {
    private var observacion: Job? = null
    private var usuarioActual: String? = null
    private var fechaConsulta: String? = null

    private val _uiState = MutableStateFlow(crearEstadoInicial())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargar(usuarioId: String) {
        val mismoUsuario = usuarioActual == usuarioId
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Calendar.getInstance().time)
        val estadoBase = crearEstadoInicial(usuarioId)
        // Volver desde un módulo no debe vaciar el gráfico ni mover las tarjetas.
        if (mismoUsuario && fechaConsulta == hoy && observacion?.isActive == true) {
            _uiState.update { it.copy(fecha = estadoBase.fecha, saludo = estadoBase.saludo) }
            return
        }
        observacion?.cancel()
        usuarioActual = usuarioId
        fechaConsulta = hoy
        _uiState.update { actual ->
            if (mismoUsuario) estadoBase.copy(actividadSemanal = actual.actividadSemanal) else estadoBase
        }

        observacion = viewModelScope.launch {
            try {
                val ahora = Calendar.getInstance()
                val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                val desde = (ahora.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }
                val hasta = (ahora.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                // Room performs the count on its query executor. Only seven daily totals reach the UI.
                dao.observarActividadDiaria(usuarioId, formato.format(desde.time), formato.format(hasta.time))
                    .map { conteos -> construirActividadDesdeConteos(conteos.associate { it.fecha to it.cantidad }, ahora) }
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { actividad ->
                        _uiState.update { it.copy(actividadSemanal = actividad, error = null) }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudo cargar la actividad semanal.")
                }
            }
        }
    }

    private fun crearEstadoInicial(usuarioId: String = ""): HomeUiState {
        val ahora = Calendar.getInstance()
        val hora = ahora.get(Calendar.HOUR_OF_DAY)

        val saludo = when (hora) {
            in 6..11 -> "Buenos días"
            in 12..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }

        val formatoFecha = SimpleDateFormat(
            "EEEE, d 'de' MMMM",
            Locale("es", "CL")
        )

        val inicialesCalculadas = if (usuarioId.isBlank()) {
            "US"
        } else {
            usuarioId.take(2).uppercase(Locale.ROOT)
        }

        return HomeUiState(
            fecha = formatoFecha.format(ahora.time),
            saludo = saludo,
            iniciales = inicialesCalculadas
        )
    }
}

// Siete días del calendario local: desde hace seis días hasta hoy, incluidos.
internal fun construirActividadSemanal(
    fechas: List<String>,
    ahora: Calendar = Calendar.getInstance()
): List<ActividadDiaUi> {
    val formatoDia = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    val cantidades = fechas.mapNotNull(::leerFechaRegistro)
        .groupingBy { formatoDia.format(it) }.eachCount()
    return construirActividadDesdeConteos(cantidades, ahora)
}

internal fun construirActividadDesdeConteos(
    cantidades: Map<String, Int>, ahora: Calendar = Calendar.getInstance()
): List<ActividadDiaUi> {
    val formatoDia = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    val etiqueta = SimpleDateFormat("EEE", Locale("es", "CL"))
    return (6 downTo 0).map { diasAtras ->
        val dia = (ahora.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -diasAtras) }
        ActividadDiaUi(
            etiqueta = if (diasAtras == 0) "Hoy" else etiqueta.format(dia.time),
            cantidad = cantidades[formatoDia.format(dia.time)] ?: 0,
            esHoy = diasAtras == 0
        )
    }
}