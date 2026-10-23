package com.kiberkot.dailysync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.repository.HistorialRepository
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
    val titulo: String,
    val simbolo: String
) {
    ANIMO("Estado de Ánimo", "🌤️"),
    DBT("Habilidades DBT", "🌊"),
    NEURODESARROLLO("Neurodesarrollo", "⚡"),
    ADICCIONES("Adicciones", "🔗")
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
    val fecha: String,
    val saludo: String,
    val iniciales: String,
    val actividadSemanal: List<ActividadDiaUi> = emptyList(),
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HistorialRepository(
        AppDatabase.getDatabase(application).dailySyncDao()
    )
    private var observacion: Job? = null

    private val _uiState = MutableStateFlow(
        crearEstadoInicial()
    )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    fun cargar(usuarioId: String) {
        observacion?.cancel()
        _uiState.value = crearEstadoInicial()
        observacion = viewModelScope.launch {
            try {
                // La misma fuente del historial: guardar o eliminar actualiza el resumen.
                repository.observarHistorial(usuarioId).collect { registros ->
                    _uiState.update {
                        crearEstadoInicial().copy(
                            actividadSemanal = construirActividadSemanal(registros.map { it.fechaHora })
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo cargar la actividad semanal.") }
            }
        }
    }

    private fun crearEstadoInicial(): HomeUiState {
        val ahora = Calendar.getInstance()
        val hora = ahora.get(Calendar.HOUR_OF_DAY)

        val saludo = when (hora) {
            in 6..11 -> "Buenos días ☀️"
            in 12..19 -> "Buenas tardes 🌤️"
            else -> "Buenas noches 🌙"
        }

        val formatoFecha = SimpleDateFormat(
            "EEEE, d 'de' MMMM",
            Locale("es", "CL")
        )

        return HomeUiState(
            fecha = formatoFecha.format(ahora.time),
            saludo = saludo,
            iniciales = "?"
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
