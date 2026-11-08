package com.kiberkot.dailysync.ui.dbt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.viewmodel.RegistroDbtUiState
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

@Composable
fun RegistroDbtScreen(
    state: RegistroDbtUiState, editando: Boolean,
    onSeleccionarHabilidad: (Int) -> Unit, onCambioSituacion: (String) -> Unit,
    onCambioIntensidad: (Int) -> Unit, onCambioUtilidad: (Int) -> Unit,
    onGuardar: () -> Unit, onReintentar: () -> Unit, onVolver: () -> Unit
) {
    BackHandler(enabled = state.procesando) { }
    val lista = rememberLazyListState()
    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current
    var elegirHabilidad by rememberSaveable { mutableStateOf(false) }
    var intento by remember { mutableIntStateOf(0) }
    var validacionMostrada by remember { mutableIntStateOf(0) }
    val disponible = !state.cargando && state.catalogo.isNotEmpty() && (!editando || state.registro != null)
    LaunchedEffect(intento, state.errores) {
        if (disponible && intento > validacionMostrada && state.errores.isNotEmpty()) {
            validacionMostrada = intento
            val indice = when {
                "habilidad" in state.errores -> 1
                "situacion" in state.errores -> 2
                "intensidad" in state.errores -> 3
                else -> 4
            }
            if (indice == 1) elegirHabilidad = true
            lista.animateScrollToItem(indice)
        }
    }
    val seleccionada = state.catalogo.find { it.id == state.formulario.habilidadId }
    DbtPagina(if (editando) "Editar práctica" else "Registrar práctica", onVolver, state.procesando, lista, pie = {
        if (disponible) DbtAccion(
            texto = when {
                state.bannerExitoVisible -> if (editando) "Cambios guardados" else "Práctica guardada"
                state.procesando -> "Guardando…"
                editando -> "Guardar cambios"
                else -> "Guardar práctica"
            }, onClick = { intento++; teclado?.hide(); foco.clearFocus(); onGuardar() },
            enabled = !state.procesando, guardando = state.procesando, error = state.error)
    }) {
        if (!disponible) item { DbtEstado(state.cargando, state.error, onReintentar) }
        else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (editando) "Revisa tu práctica" else "¿Cómo te fue al practicar?", style = textoDbt(25, FontWeight.Bold))
                    Text("Registra la situación y lo que te aportó la habilidad.", style = textoDbt(14), color = Brand.Muted)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DbtPaso("1", "Habilidad practicada")
                    DbtTarjeta {
                        if (seleccionada != null) {
                            val guia = guiaDbt(seleccionada)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Image(painterResource(guia.icono), null, Modifier.size(44.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(seleccionada.nombre, style = textoDbt(17, FontWeight.SemiBold))
                                    Text(guia.modulo, style = textoDbt(12), color = guia.acento)
                                }
                            }
                            Text(guia.resumen, style = textoDbt(14), color = Brand.Muted)
                        }
                        TextButton(onClick = { elegirHabilidad = !elegirHabilidad }, enabled = !state.procesando,
                            contentPadding = PaddingValues(0.dp)) {
                            Text(if (elegirHabilidad && seleccionada != null) "Cerrar selección" else if (seleccionada == null)
                                "Elegir habilidad" else "Cambiar habilidad", style = textoDbt(14, FontWeight.Medium), color = Brand.Teal)
                        }
                        if (elegirHabilidad || seleccionada == null) {
                            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.catalogo.forEach { habilidad ->
                                    val guia = guiaDbt(habilidad)
                                    val selected = seleccionada?.id == habilidad.id
                                    Surface(shape = RoundedCornerShape(16.dp), color = if (selected) guia.fondo else Brand.Field, contentColor = Brand.Ink,
                                        border = BorderStroke(1.dp, if (selected) guia.acento else Brand.Border),
                                        modifier = Modifier.fillMaxWidth().selectable(selected, enabled = !state.procesando,
                                            role = Role.RadioButton, onClick = { onSeleccionarHabilidad(habilidad.id); elegirHabilidad = false })
                                            .testTag("dbt_habilidad_${habilidad.id}")) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Image(painterResource(guia.icono), null, Modifier.size(32.dp))
                                            Text(habilidad.nombre, style = textoDbt(15, FontWeight.Medium), modifier = Modifier.weight(1f))
                                            RadioButton(selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = guia.acento))
                                        }
                                    }
                                }
                            }
                        }
                        state.errores["habilidad"]?.let { DbtMensajeError(it) }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DbtPaso("2", "Situación o emoción")
                    DbtTarjeta {
                        Text("¿Qué estaba pasando?", style = textoDbt(15, FontWeight.SemiBold))
                        OutlinedTextField(value = state.formulario.situacion, onValueChange = onCambioSituacion,
                            enabled = !state.procesando, minLines = 3, maxLines = 6, textStyle = textoDbt(16),
                            placeholder = { Text("Ej. Quería responder de inmediato a un mensaje", style = textoDbt(14), color = Brand.Muted) },
                            shape = RoundedCornerShape(18.dp), isError = "situacion" in state.errores,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Brand.Field,
                                unfocusedContainerColor = Brand.Field, errorContainerColor = Brand.Field,
                                focusedBorderColor = Brand.Teal, unfocusedBorderColor = Brand.Border,
                                focusedTextColor = Brand.Ink, unfocusedTextColor = Brand.Ink, cursorColor = Brand.Teal,
                                errorBorderColor = Brand.Error),
                            modifier = Modifier.fillMaxWidth().testTag("dbt_situacion").semantics { contentDescription = "Situación o emoción" })
                        state.errores["situacion"]?.let { DbtMensajeError(it) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Entre 3 y 250 caracteres", style = textoDbt(12), color = Brand.Muted)
                            Text("${state.formulario.situacion.trim().length}/250", style = textoDbt(12),
                                color = if (state.formulario.situacion.trim().length > 250) Brand.Error else Brand.Muted)
                        }
                    }
                }
            }
            item {
                DbtEscala("3", "Intensidad de la emoción", "¿Cuánto la sentiste?", state.formulario.intensidad,
                    "Suave", "Muy intensa", Color(0xFFFAEEE5), Color(0xFF95613F), "intensidad", !state.procesando,
                    state.errores["intensidad"], onCambioIntensidad)
            }
            item {
                DbtEscala("4", "Cuánto te ayudó", "¿Qué tan útil fue la habilidad?", state.formulario.utilidad,
                    "Poco útil", "Muy útil", Color(0xFFEAF5EF), Color(0xFF467560), "utilidad", !state.procesando,
                    state.errores["utilidad"], onCambioUtilidad)
            }
        }
    }
}

@Composable
internal fun DbtEscala(numero: String, titulo: String, pregunta: String, valor: Int,
    minimo: String, maximo: String, fondo: Color, acento: Color, tag: String, enabled: Boolean,
    error: String?, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DbtPaso(numero, titulo)
        DbtTarjeta {
            Text(pregunta, style = textoDbt(14), color = Brand.Muted)
            Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { nivel ->
                    val selected = valor == nivel
                    Surface(shape = RoundedCornerShape(14.dp), color = if (selected) acento else fondo,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = { onChange(nivel) })
                            .testTag("dbt_${tag}_$nivel")) {
                        Box(Modifier.padding(vertical = 10.dp).clearAndSetSemantics {
                            contentDescription = "$titulo: $nivel de 5"
                        }, contentAlignment = Alignment.Center) {
                            Text("$nivel", style = textoDbt(18, FontWeight.SemiBold), color = if (selected) Color.White else acento)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(minimo, style = textoDbt(12), color = Brand.Muted)
                Text(maximo, style = textoDbt(12), color = Brand.Muted)
            }
            error?.let { DbtMensajeError(it) }
        }
    }
}
