package com.kiberkot.dailysync.ui.dbt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.viewmodel.DbtUiState
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogoDbtScreen(state: DbtUiState, onSeleccionarModulo: (String?) -> Unit,
    onVerHabilidad: (Int) -> Unit, onReintentar: () -> Unit, onVolver: () -> Unit) {
    DbtPagina("Catálogo", onVolver) {
        item {
            DbtTarjeta(color = DbtLavender) {
                Text("Elige una habilidad", style = textoDbt(25, FontWeight.Bold))
                Text("Explora, elige y registra cómo te fue.", style = textoDbt(14), color = Brand.Muted)
                Text("${state.catalogo.size} habilidades a tu alcance", style = textoDbt(12, FontWeight.Medium), color = DbtPurple)
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FiltroDbt("Todas", state.moduloSeleccionado == null, DbtLavender, DbtPurple) { onSeleccionarModulo(null) }
                state.modulosDisponibles.forEach { modulo ->
                    val guia = guiaDbt(state.catalogo.first { it.modulo == modulo })
                    FiltroDbt(guia.modulo, state.moduloSeleccionado == modulo, guia.fondo, guia.acento) { onSeleccionarModulo(modulo) }
                }
            }
        }
        when {
            state.cargando || state.error != null -> item { DbtEstado(state.cargando, state.error, onReintentar) }
            state.habilidadesVisibles.isEmpty() -> item { Text("No hay habilidades en esta selección.", style = textoDbt(14)) }
            else -> items(state.habilidadesVisibles, key = { it.id }) { habilidad ->
                val guia = guiaDbt(habilidad)
                Surface(onClick = { onVerHabilidad(habilidad.id) }, shape = RoundedCornerShape(24.dp),
                    color = Color.White, contentColor = Brand.Ink, border = BorderStroke(1.dp, Brand.Border), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = RoundedCornerShape(16.dp), color = guia.fondo) {
                                Image(painterResource(guia.icono), null, Modifier.size(52.dp).padding(4.dp))
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(guia.modulo, style = textoDbt(12, FontWeight.Medium), color = guia.acento)
                                Text(habilidad.nombre, style = textoDbt(19, FontWeight.SemiBold))
                            }
                            Icon(painterResource(R.drawable.ic_home_arrow), null, Modifier.size(20.dp), tint = guia.acento)
                        }
                        Text(guia.resumen, style = textoDbt(15), color = Brand.Muted)
                        Text("Ver habilidad", style = textoDbt(13, FontWeight.SemiBold), color = guia.acento)
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroDbt(texto: String, selected: Boolean, fondo: Color, acento: Color, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, selectedContainerColor = fondo,
            labelColor = Brand.Muted, selectedLabelColor = acento),
        border = BorderStroke(1.dp, if (selected) acento else Brand.Border),
        label = { Text(texto, style = textoDbt(13, if (selected) FontWeight.SemiBold else FontWeight.Normal)) })
}
