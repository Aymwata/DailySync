package com.kiberkot.dailysync.ui.dbt

import com.kiberkot.dailysync.ui.components.AvisoRegistroGuardado

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.viewmodel.RegistroDbtUiState
import com.kiberkot.dailysync.ui.components.DbtScaffold
import com.kiberkot.dailysync.ui.components.DbtSeccion
import com.kiberkot.dailysync.ui.components.DbtError

@Composable
fun RegistroDbtScreen(
    state: RegistroDbtUiState, editando: Boolean,
    onSeleccionarHabilidad: (Int) -> Unit, onCambioSituacion: (String) -> Unit,
    onCambioIntensidad: (Int) -> Unit, onCambioUtilidad: (Int) -> Unit,
    onGuardar: () -> Unit, onReintentar: () -> Unit, onVolver: () -> Unit
) {
    BackHandler(enabled = state.procesando) { /* Terminar la escritura antes de salir. */ }
    DbtScaffold(if (editando) "Editar práctica" else "Registrar práctica", onVolver, state.procesando) {
        when {
            state.cargando -> item { CircularProgressIndicator() }
            state.catalogo.isEmpty() || (editando && state.registro == null) -> item {
                DbtError(state.error ?: "No hay datos disponibles para esta práctica.", onReintentar)
            }
            else -> {
                item { Text("Utiliza una situación ficticia. La práctica se guardará en este dispositivo.", style = MaterialTheme.typography.bodySmall) }
                state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
                item {
                    DbtSeccion("1. Habilidad practicada") {
                        state.catalogo.forEach { habilidad ->
                            FilterChip(selected = state.formulario.habilidadId == habilidad.id,
                                onClick = { onSeleccionarHabilidad(habilidad.id) }, enabled = !state.procesando,
                                label = { Text(habilidad.nombre) }, modifier = Modifier.fillMaxWidth())
                        }
                        state.errores["habilidad"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }
                item {
                    DbtSeccion("2. Situación o emoción asociada") {
                        OutlinedTextField(value = state.formulario.situacion, onValueChange = onCambioSituacion,
                            enabled = !state.procesando, label = { Text("Describe una situación ficticia") },
                            minLines = 3, isError = "situacion" in state.errores,
                            supportingText = { Text(state.errores["situacion"]
                                ?: "${state.formulario.situacion.trim().length}/250 caracteres · mínimo 3") },
                            modifier = Modifier.fillMaxWidth().testTag("dbt_situacion"))
                    }
                }
                item {
                    DbtSeccion("3. Intensidad: ${state.formulario.intensidad}/5") {
                        Slider(value = state.formulario.intensidad.toFloat(),
                            onValueChange = { onCambioIntensidad(it.toInt()) }, valueRange = 1f..5f, steps = 3,
                            enabled = !state.procesando)
                        Text("1 = mínima · 5 = máxima", style = MaterialTheme.typography.bodySmall)
                        state.errores["intensidad"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }
                item {
                    DbtSeccion("4. Utilidad percibida: ${state.formulario.utilidad}/5") {
                        Slider(value = state.formulario.utilidad.toFloat(),
                            onValueChange = { onCambioUtilidad(it.toInt()) }, valueRange = 1f..5f, steps = 3,
                            enabled = !state.procesando)
                        Text("1 = poco útil · 5 = muy útil", style = MaterialTheme.typography.bodySmall)
                        state.errores["utilidad"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }
                item {
                    AvisoRegistroGuardado(
                        visible = state.bannerExitoVisible, editando = editando
                    )
                }
                item {
                    Button(onClick = onGuardar, enabled = !state.procesando,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text(if (state.procesando) "Guardando…" else if (editando) "Guardar cambios" else "Guardar práctica")
                    }
                }
            }
        }
    }
}
