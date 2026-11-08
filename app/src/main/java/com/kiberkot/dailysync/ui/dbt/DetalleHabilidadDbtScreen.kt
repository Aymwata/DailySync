package com.kiberkot.dailysync.ui.dbt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.viewmodel.DbtUiState
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

@Composable
fun DetalleHabilidadDbtScreen(state: DbtUiState, habilidadId: Int, onRegistrarPractica: (Int) -> Unit,
    onReintentar: () -> Unit, onVolver: () -> Unit) {
    val habilidad = state.catalogo.find { it.id == habilidadId }
    DbtPagina("Conoce la habilidad", onVolver, pie = {
        if (!state.cargando && state.error == null && habilidad != null) {
            DbtAccion("Registrar práctica", { onRegistrarPractica(habilidad.id) })
        }
    }) {
        when {
            state.cargando || state.error != null || habilidad == null -> item {
                DbtEstado(state.cargando, state.error ?: "No se encontró la habilidad.", onReintentar)
            }
            else -> {
                val guia = guiaDbt(habilidad)
                item {
                    DbtTarjeta(color = guia.fondo) {
                        Image(painterResource(guia.icono), null, Modifier.size(64.dp))
                        Text(guia.modulo, style = textoDbt(12, FontWeight.SemiBold), color = guia.acento)
                        Text(habilidad.nombre, style = textoDbt(27, FontWeight.Bold))
                        Text(guia.resumen, style = textoDbt(16), color = Brand.Ink)
                    }
                }
                if (guia.pasos.isNotEmpty()) item {
                    DbtTarjeta {
                        Text("Pruébala así", style = textoDbt(18, FontWeight.SemiBold))
                        guia.pasos.forEachIndexed { indice, paso ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                Surface(shape = RoundedCornerShape(10.dp), color = guia.fondo) {
                                    Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                                        Text("${indice + 1}", style = textoDbt(13, FontWeight.SemiBold), color = guia.acento)
                                    }
                                }
                                Text(paso, style = textoDbt(15), modifier = Modifier.weight(1f).padding(top = 3.dp))
                            }
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Al registrar", style = textoDbt(15, FontWeight.SemiBold), color = guia.acento)
                        Text("Cuenta la situación y valora la intensidad y cuánto te ayudó.", style = textoDbt(14), color = Brand.Muted)
                    }
                }
            }
        }
    }
}
