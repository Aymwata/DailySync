package com.kiberkot.dailysync.ui.dbt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.kiberkot.dailysync.viewmodel.RegistroDbtUiState
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

@Composable
fun DetalleRegistroDbtScreen(
    state: RegistroDbtUiState,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    onEliminar: () -> Unit = {},
    onVerHistorial: () -> Unit = {},
    onReintentar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val registro = state.registro
    val habilidad = state.catalogo.find { it.id == registro?.habilidadId }
    val guia = habilidad?.let(::guiaDbt)
    DbtPagina("Detalle de Práctica DBT", onVolver, modifier = modifier, pie = {
        if (!state.cargando && state.error == null && registro != null) DbtAccion("Editar Registro", onEditar)
    }) {
        when {
            state.cargando || state.error != null || registro == null -> item {
                DbtEstado(state.cargando, state.error, onReintentar)
            }
            else -> {
                item {
                    DbtTarjeta(color = guia?.fondo ?: DbtLavender) {
                        Image(painterResource(guia?.icono ?: R.drawable.ic_module_dbt), null, Modifier.size(56.dp))
                        Text("Habilidad practicada", style = textoDbt(12, FontWeight.Medium), color = guia?.acento ?: DbtPurple)
                        if (registro.editado) Text("Editado", style = textoDbt(12, FontWeight.SemiBold), color = Brand.Teal)
                        Text(registro.habilidadNombre, style = textoDbt(25, FontWeight.Bold))
                        Text(guia?.modulo ?: registro.modulo, style = textoDbt(14), color = Brand.Muted)
                    }
                }
                item {
                    DbtTarjeta {
                        Text("Situación o emoción", style = textoDbt(17, FontWeight.SemiBold))
                        Text(registro.situacion.ifBlank { "Sin contexto añadido" }, style = textoDbt(16), color = Brand.Muted)
                    }
                }
                item { DbtValorRegistrado("Intensidad de la emoción", registro.intensidad,
                    Color(0xFFFAEEE5), Color(0xFF95613F), "Sin síntomas", "Severa", registro.versionEscala) }
                item { DbtValorRegistrado("Cuánto te ayudó", registro.utilidad,
                    Color(0xFFEAF5EF), Color(0xFF467560), "Poco útil", "Muy útil") }
            }
        }
    }
}

@Composable
private fun DbtValorRegistrado(titulo: String, valor: Int, fondo: Color, acento: Color, minimo: String, maximo: String, versionEscala: Int? = null) {
    DbtTarjeta {
        Text(titulo, style = textoDbt(16, FontWeight.SemiBold))
        Text(if (versionEscala == null) "$valor/5" else com.kiberkot.dailysync.model.textoIntensidad(valor, versionEscala),
            style = textoDbt(20, FontWeight.Bold), color = acento)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(if (versionEscala == 2) 4 else 5) { indice ->
                Surface(modifier = Modifier.weight(1f).height(8.dp), shape = RoundedCornerShape(4.dp),
                    color = if (indice < valor) acento else fondo) { }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (versionEscala == 1) "Suave" else minimo, style = textoDbt(12), color = Brand.Muted)
            Text(if (versionEscala == 1) "Muy intensa" else maximo, style = textoDbt(12), color = Brand.Muted)
        }
    }
}
