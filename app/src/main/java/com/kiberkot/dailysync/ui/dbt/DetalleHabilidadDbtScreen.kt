package com.kiberkot.dailysync.ui.dbt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.viewmodel.DbtUiState
import com.kiberkot.dailysync.ui.components.DbtScaffold
import com.kiberkot.dailysync.ui.components.DbtSeccion
import com.kiberkot.dailysync.ui.components.DbtError

@Composable
fun DetalleHabilidadDbtScreen(state: DbtUiState, habilidadId: Int, onRegistrarPractica: (Int) -> Unit,
    onReintentar: () -> Unit, onVolver: () -> Unit) {
    val habilidad = state.catalogo.find { it.id == habilidadId }
    DbtScaffold(habilidad?.nombre ?: "Detalle de habilidad", onVolver) {
        when {
            state.cargando -> item { CircularProgressIndicator() }
            state.error != null -> item { DbtError(state.error, onReintentar) }
            habilidad == null -> item { Text("No se encontró la habilidad solicitada.") }
            else -> {
                item { DbtSeccion(habilidad.nombre) { Text(habilidad.modulo, color = MaterialTheme.colorScheme.primary) } }
                item { DbtSeccion("Contenido de la habilidad") { Text(habilidad.contenido) } }
                item {
                    Button(onClick = { onRegistrarPractica(habilidad.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text("Registrar práctica de esta habilidad")
                    }
                }
                item { Text("Contenido educativo de referencia. Utiliza situaciones ficticias en el registro.", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
