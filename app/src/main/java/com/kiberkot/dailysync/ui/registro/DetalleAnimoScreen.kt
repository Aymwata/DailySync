package com.kiberkot.dailysync.ui.registro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.model.textoIntensidad
import com.kiberkot.dailysync.ui.components.*
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.utils.formatearFechaRegistro
import com.kiberkot.dailysync.viewmodel.AnimoUiState

@Composable
fun DetalleAnimoScreen(
    state: AnimoUiState,
    registroId: String,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    onReintentar: () -> Unit = {}
) {
    val registro = state.historial.find { it.id == registroId }
    val disponible = !state.cargando && !state.cargaFallida && !state.noDisponible && registro != null
    Scaffold(modifier = modifier.fillMaxSize(), containerColor = FondoPantallaMarca,
        topBar = {
            CabeceraMarca("Detalle del registro", "Estado de ánimo", onVolver,
                R.drawable.ic_module_animo, fondoIcono = AnimoSoft, tinteIcono = null)
        }, bottomBar = {
            if (disponible) PieMarca {
                AccionMarca("Editar Registro", onEditar, icono = R.drawable.ic_form_note)
            }
        }) { padding ->
        if (!disponible) {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                TarjetaMarca {
                    if (state.cargando) {
                        CircularProgressIndicator(color = Brand.Teal, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text("Cargando registro…", style = textoMarca(14), color = Brand.Muted)
                    } else {
                        Text(state.error ?: "No se encontró el registro solicitado.", style = textoMarca(14), color = Brand.Error)
                        TextButton(onClick = onReintentar) { Text("Reintentar", style = textoMarca(14, FontWeight.Medium), color = Brand.Teal) }
                    }
                }
            }
            return@Scaffold
        }
        val estilo = estiloAnimo(registro.categoriaId, registro.categoriaNombre)
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            TarjetaMarca(fondo = estilo.fondo) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painterResource(estilo.icono), null, Modifier.size(48.dp))
                    Text("Estado emocional", style = textoMarca(12, FontWeight.Medium), color = estilo.acento,
                        modifier = Modifier.weight(1f))
                    if (registro.editado) Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.8f)) {
                        Text("Editado", style = textoMarca(12, FontWeight.SemiBold), color = estilo.acento,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
                Text(registro.categoriaNombre, style = textoMarca(25, FontWeight.Bold), color = Brand.Ink)
            }
            TarjetaMarca {
                Text("Fecha y hora del registro", style = textoMarca(12, FontWeight.Medium), color = Brand.Muted)
                Text(remember(registro.fechaHora) { formatearFechaRegistro(registro.fechaHora) },
                    style = textoMarca(15, FontWeight.SemiBold), color = Brand.Ink)
            }
            TarjetaMarca {
                Text("Nivel de intensidad", style = textoMarca(17, FontWeight.SemiBold), color = Brand.Ink)
                Text(textoIntensidad(registro.nivelIntensidad, registro.versionEscala),
                    style = textoMarca(14, FontWeight.Medium), color = estilo.acento)
                Row(Modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (if (registro.versionEscala == 1) 1..5 else 0..4).forEach { nivel ->
                        val seleccionado = registro.nivelIntensidad == nivel
                        Surface(shape = RoundedCornerShape(14.dp), color = if (seleccionado) estilo.acento else estilo.fondo,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text("$nivel", style = textoMarca(18, FontWeight.SemiBold),
                                    color = if (seleccionado) Color.White else estilo.acento)
                            }
                        }
                    }
                }
            }
            TarjetaMarca {
                Text("Contexto", style = textoMarca(17, FontWeight.SemiBold), color = Brand.Ink)
                Text(registro.contexto.ifBlank { "Sin contexto registrado" }, style = textoMarca(15), color = Brand.Muted)
            }
            TarjetaMarca {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.ic_form_note), null, Modifier.size(20.dp), tint = estilo.acento)
                    Text("Nota opcional", style = textoMarca(17, FontWeight.SemiBold), color = Brand.Ink)
                }
                Text(registro.notaOpcional?.takeIf { it.isNotBlank() } ?: "Sin nota", style = textoMarca(15), color = Brand.Muted)
            }
        }
    }
}
