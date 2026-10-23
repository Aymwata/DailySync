package com.kiberkot.dailysync.ui.dbt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.viewmodel.DbtUiState
import com.kiberkot.dailysync.ui.components.DbtScaffold
import com.kiberkot.dailysync.ui.components.DbtError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoDbtScreen(state: DbtUiState, onSeleccionarModulo: (String?) -> Unit,
    onVerHabilidad: (Int) -> Unit, onReintentar: () -> Unit, onVolver: () -> Unit) {
    DbtScaffold("Catálogo", onVolver) {
        item {
            Text("Catálogo local · ${state.catalogo.size} habilidades", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = state.moduloSeleccionado == null,
                    onClick = { onSeleccionarModulo(null) }, label = { Text("Todas") }) }
                items(state.modulosDisponibles) { modulo ->
                    FilterChip(selected = state.moduloSeleccionado == modulo,
                        onClick = { onSeleccionarModulo(modulo) }, label = { Text(modulo) })
                }
            }
        }
        when {
            state.cargando -> item { CircularProgressIndicator() }
            state.error != null -> item { DbtError(state.error, onReintentar) }
            state.habilidadesVisibles.isEmpty() -> item { Text("No hay habilidades para esta selección.") }
            else -> items(state.habilidadesVisibles, key = { it.id }) { habilidad ->
                ElevatedCard(onClick = { onVerHabilidad(habilidad.id) }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(habilidad.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(habilidad.modulo, color = MaterialTheme.colorScheme.primary)
                        Text(habilidad.contenido, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text("Ver habilidad →", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
