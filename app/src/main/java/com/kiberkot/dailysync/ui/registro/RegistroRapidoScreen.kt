package com.kiberkot.dailysync.ui.registro

import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kiberkot.dailysync.ui.adiccion.AdiccionScreen
import com.kiberkot.dailysync.ui.components.RegistroRapidoConfig
import com.kiberkot.dailysync.ui.dbt.RegistroDbtScreen
import com.kiberkot.dailysync.ui.neuro.NeuroScreen
import com.kiberkot.dailysync.viewmodel.*
import kotlinx.coroutines.delay

/** Un destino con cuatro pasos: los formularios mantienen su persistencia independiente. */
@Composable
fun RegistroRapidoScreen(usuarioId: String, onFinalizar: () -> Unit) {
    var paso by rememberSaveable { mutableIntStateOf(0) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    var finalizando by remember { mutableStateOf(false) }
    var operacionEnCurso: () -> Boolean = { false }
    val solicitarSalida = {
        if (!operacionEnCurso() && !finalizando) confirmarSalida = true
    }
    val avanzar: (Int) -> Unit = { origen ->
        // Descarta eventos tardíos o pulsaciones repetidas del formulario que ya salió.
        if (paso == origen && !finalizando) {
            if (origen == 3) { finalizando = true; onFinalizar() }
            else paso = origen + 1
        }
    }
    BackHandler { solicitarSalida() }
    key(paso) {
        when (paso) {
            0 -> {
                val vm: AnimoViewModel = viewModel(key = "rapido_animo")
                operacionEnCurso = { vm.uiState.value.guardando }
                val state by vm.uiState.collectAsState()
                LaunchedEffect(usuarioId) { vm.cargar(usuarioId) }
                RegistroAnimoScreen(state = state,
                    onSeleccionarCategoria = vm::seleccionarCategoria,
                    onCambioIntensidad = vm::cambiarIntensidad, onCambioContexto = vm::cambiarContexto,
                    onCambioNota = vm::cambiarNota, onReintentar = { vm.cargar(usuarioId) },
                    onGuardar = { vm.guardarRegistro { avanzar(0) } }, onVolver = solicitarSalida,
                    registroRapido = RegistroRapidoConfig(1) {
                        if (!vm.uiState.value.guardando) avanzar(0)
                    })
            }
            1 -> {
                val vm: RegistroDbtViewModel = viewModel(key = "rapido_dbt")
                operacionEnCurso = { vm.uiState.value.procesando }
                val state by vm.uiState.collectAsState()
                // Sin habilidad preseleccionada: el mismo formulario permite elegirla.
                LaunchedEffect(usuarioId) { vm.cargar(usuarioId) }
                RegistroDbtScreen(state = state, editando = false,
                    onSeleccionarHabilidad = vm::seleccionarHabilidad, onCambioSituacion = vm::cambiarSituacion,
                    onCambioIntensidad = vm::cambiarIntensidad, onCambioUtilidad = vm::cambiarUtilidad,
                    onGuardar = { vm.guardar { avanzar(1) } }, onReintentar = { vm.cargar(usuarioId) },
                    onVolver = solicitarSalida, registroRapido = RegistroRapidoConfig(2) {
                        if (!vm.uiState.value.procesando) avanzar(1)
                    })
            }
            2 -> {
                val vm: NeuroViewModel = viewModel(key = "rapido_neuro")
                operacionEnCurso = { vm.uiState.value.guardando || vm.uiState.value.guardadoExitoso }
                val state by vm.uiState.collectAsState()
                LaunchedEffect(usuarioId) { vm.cargar(usuarioId) }
                LaunchedEffect(state.guardadoExitoso) {
                    if (state.guardadoExitoso) { delay(1000); avanzar(2) }
                }
                NeuroScreen(state = state, onCambioTipoActividad = vm::cambiarTipoActividad,
                    onCambioSobrecarga = vm::cambiarNivelSobrecarga, onSeleccionarEstrategia = vm::seleccionarEstrategia,
                    onCambioContexto = vm::cambiarContexto, onCambioResultado = vm::cambiarResultadoPercibido,
                    onGuardar = { vm.guardarRegistro(usuarioId) }, onReintentar = { vm.cargar(usuarioId) },
                    onVolver = solicitarSalida, registroRapido = RegistroRapidoConfig(3) {
                        val actual = vm.uiState.value
                        if (!actual.guardando && !actual.guardadoExitoso) avanzar(2)
                    })
            }
            3 -> {
                val vm: AdiccionViewModel = viewModel(key = "rapido_adiccion")
                operacionEnCurso = { vm.uiState.value.guardando || vm.uiState.value.guardadoExitoso }
                val state by vm.uiState.collectAsState()
                LaunchedEffect(usuarioId) { vm.cargar(usuarioId) }
                LaunchedEffect(state.guardadoExitoso, state.mostrarApoyo) {
                    if (state.guardadoExitoso && !state.mostrarApoyo) { delay(1000); avanzar(3) }
                }
                AdiccionScreen(state = state, onCambioImpulso = vm::cambiarNivelImpulso,
                    onCambioDesencadenante = vm::cambiarDesencadenante, onSeleccionarAccion = vm::seleccionarAccion,
                    onSeleccionarDesencadenante = vm::seleccionarDesencadenante, onCambioOtraAccion = vm::cambiarOtraAccion,
                    onCambioEstadoObjetivo = vm::cambiarEstadoObjetivo, onCerrarApoyo = vm::cerrarApoyo,
                    onGuardar = { vm.guardarRegistro(usuarioId) }, onReintentar = { vm.cargar(usuarioId) },
                    onVolver = solicitarSalida, registroRapido = RegistroRapidoConfig(4) {
                        val actual = vm.uiState.value
                        if (!actual.guardando && !actual.guardadoExitoso) avanzar(3)
                    })
            }
        }
    }
    if (confirmarSalida) AlertDialog(onDismissRequest = { confirmarSalida = false },
        title = { Text("¿Salir del registro rápido?") },
        text = { Text("Los registros que ya guardaste se conservarán. El formulario actual se descartará si todavía no lo has guardado.") },
        confirmButton = { TextButton(onClick = {
            if (!finalizando) { finalizando = true; onFinalizar() }
        }) { Text("Salir al inicio") } },
        dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Seguir registrando") } })
}
