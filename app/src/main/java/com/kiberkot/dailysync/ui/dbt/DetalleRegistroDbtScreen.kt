package com.kiberkot.dailysync.ui.dbt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.viewmodel.RegistroDbtUiState
import com.kiberkot.dailysync.ui.components.DbtScaffold
import com.kiberkot.dailysync.ui.components.DbtSeccion
import com.kiberkot.dailysync.ui.components.DbtError
import com.kiberkot.dailysync.utils.formatearFechaRegistro

@Composable
fun DetalleRegistroDbtScreen(state: RegistroDbtUiState, onEditar: () -> Unit, onEliminar: () -> Unit,
    onVerHistorial: () -> Unit, onReintentar: () -> Unit, onVolver: () -> Unit) {
    val registro = state.registro
    var confirmarEliminar by rememberSaveable(registro?.id) { mutableStateOf(false) }
    BackHandler(enabled = state.procesando) { }
    DbtScaffold("Detalle de práctica", onVolver, state.procesando) {
        when {
            state.cargando -> item { CircularProgressIndicator() }
            registro == null -> item { DbtError(state.error ?: "No se encontró la práctica.", onReintentar) }
            else -> {
                state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
                item { DbtSeccion(registro.habilidadNombre) { Text(registro.modulo); Text(formatearFechaRegistro(registro.fechaHora)) } }
                item { DbtSeccion("Situación o emoción asociada") { Text(registro.situacion) } }
                item {
                    DbtSeccion("Práctica registrada") {
                        Text("Intensidad: ${registro.intensidad}/5")
                        Text("Utilidad percibida: ${registro.utilidad}/5")
                        Text(if (registro.pendienteSync) "Guardado localmente · Pendiente de sincronización" else "Sincronizado")
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onEditar, enabled = !state.procesando, modifier = Modifier.fillMaxWidth()) { Text("Editar práctica") }
                        OutlinedButton(onClick = { confirmarEliminar = true }, enabled = !state.procesando,
                            modifier = Modifier.fillMaxWidth()) { Text("Eliminar práctica", color = MaterialTheme.colorScheme.error) }
                        TextButton(onClick = onVerHistorial, enabled = !state.procesando, modifier = Modifier.fillMaxWidth()) { Text("Ver historial") }
                    }
                }
            }
        }
    }
    if (confirmarEliminar && registro != null) {
        AlertDialog(onDismissRequest = { confirmarEliminar = false }, title = { Text("Eliminar práctica") },
            text = { Text("Esta práctica se eliminará del dispositivo. Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { confirmarEliminar = false; onEliminar() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } })
    }
}
