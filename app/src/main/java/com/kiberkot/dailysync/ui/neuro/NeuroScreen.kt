package com.kiberkot.dailysync.ui.neuro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.data.local.entities.CatalogoNeuroEntity
import com.kiberkot.dailysync.ui.components.AvisoRegistroGuardado
import com.kiberkot.dailysync.ui.components.EstadoRegistro
import com.kiberkot.dailysync.ui.theme.BarrasClarasMarca
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.viewmodel.NeuroUiState

private val NeuroAccent = Color(0xFF466F97)
private val NeuroSoft = Color(0xFFE8F2FA)

private fun textoNeuro(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Brand.Font,
    fontSize = size.sp,
    lineHeight = (size * 1.35f).sp,
    fontWeight = weight
)

@Composable
fun NeuroScreen(
    state: NeuroUiState,
    editando: Boolean = false,
    onCambioTipoActividad: (String) -> Unit,
    onCambioSobrecarga: (Int) -> Unit,
    onSeleccionarEstrategia: (Int) -> Unit,
    onCambioContexto: (String) -> Unit,
    onCambioResultado: (Int) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    onReintentar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    BackHandler(enabled = state.guardando || state.guardadoExitoso) { }
    BarrasClarasMarca()

    val disponible = !state.isLoading && !state.cargaFallida && !state.noDisponible
    val guardar = {
        keyboard?.hide()
        focus.clearFocus()
        onGuardar()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = Color(0xFFF6FAF7),
        contentColor = Brand.Ink,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = Color.White) {
                    IconButton(
                        onClick = onVolver,
                        enabled = !state.guardando && !state.guardadoExitoso,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_form_back),
                            contentDescription = "Volver",
                            tint = Brand.Ink,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("Neurodesarrollo", style = textoNeuro(12, FontWeight.Medium), color = NeuroAccent)
                    Text(
                        if (editando) "Editar Autorregulación" else "Nueva Autorregulación",
                        style = textoNeuro(20, FontWeight.SemiBold),
                        color = Brand.Ink
                    )
                }
                Surface(shape = RoundedCornerShape(14.dp), color = NeuroSoft) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_module_neuro),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (disponible) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.errorMsg?.let { ErrorNeuro(it) }

                        AvisoRegistroGuardado(visible = state.guardadoExitoso, editando = editando)

                        val shape = RoundedCornerShape(18.dp)
                        Button(
                            onClick = guardar,
                            enabled = !state.guardando && !state.guardadoExitoso,
                            shape = shape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                contentColor = Color.White,
                                disabledContentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), shape)
                        ) {
                            when {
                                state.guardadoExitoso -> Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp))
                                state.guardando -> CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else -> Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = when {
                                    state.guardadoExitoso -> if (editando) "Cambios guardados" else "Registro guardado"
                                    state.guardando -> "Guardando…"
                                    editando -> "Guardar cambios"
                                    else -> "Guardar registro"
                                },
                                style = textoNeuro(16, FontWeight.SemiBold)
                            )
                        }
                        Text("Se guarda en este dispositivo", style = textoNeuro(12), color = Brand.Muted)
                    }
                }
            }
        }
    ) { padding ->
        if (!disponible) {
            EstadoRegistro(state.isLoading, state.errorMsg, onReintentar, Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (editando) "Revisa tu autorregulación" else "Registro de Autorregulación",
                    style = textoNeuro(25, FontWeight.Bold),
                    color = Brand.Ink
                )
                Text(
                    "Monitorea la sobrecarga sensorial y evalúa tus estrategias.",
                    style = textoNeuro(14),
                    color = Brand.Muted
                )
            }


            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoNeuro("1", "Tipo de actividad o tarea")
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("¿Qué estabas realizando?", style = textoNeuro(13), color = Brand.Muted)
                        OutlinedTextField(
                            enabled = !state.guardando && !state.guardadoExitoso,
                            value = state.tipoActividad,
                            onValueChange = onCambioTipoActividad,
                            textStyle = textoNeuro(16),
                            placeholder = { Text("Ej. Estudio intenso, reunión ruidosa", style = textoNeuro(14), color = Color(0xFF7A9389)) },
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Brand.Field, unfocusedContainerColor = Brand.Field,
                                focusedBorderColor = Brand.Teal, unfocusedBorderColor = Brand.Border,
                                focusedTextColor = Brand.Ink, unfocusedTextColor = Brand.Ink
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoNeuro("2", "Sobrecarga sensorial")
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nivel de sobrecarga", style = textoNeuro(14, FontWeight.Medium), color = Brand.Ink)
                            Text(descripcionNivel(state.nivelSobrecarga), style = textoNeuro(12, FontWeight.SemiBold), color = NeuroAccent)
                        }
                        Row(
                            Modifier.fillMaxWidth().selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { nivel ->
                                val seleccionada = state.nivelSobrecarga == nivel
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (seleccionada) NeuroAccent else Color(0xFFF5F7F3),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .selectable(
                                            selected = seleccionada,
                                            enabled = !state.guardando && !state.guardadoExitoso,
                                            role = Role.RadioButton,
                                            onClick = { onCambioSobrecarga(nivel) }
                                        )
                                ) {
                                    Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            "$nivel",
                                            style = textoNeuro(18, FontWeight.SemiBold),
                                            color = if (seleccionada) Color.White else Brand.Muted
                                        )
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Baja / Nula", style = textoNeuro(12), color = Brand.Muted)
                            Text("Extrema", style = textoNeuro(12), color = Brand.Muted)
                        }
                    }
                }
            }


            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoNeuro("3", "Elige una estrategia")
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.catalogoEstrategias.forEach { est ->
                        OpcionEstrategiaNeuro(
                            estrategia = est,
                            seleccionada = state.idEstrategiaSeleccionada == est.idEstrategia,
                            enabled = !state.guardando && !state.guardadoExitoso,
                            onSelect = { onSeleccionarEstrategia(est.idEstrategia) }
                        )
                    }
                }
            }


            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoNeuro("4", "Añade contexto o entorno")
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("¿Dónde estabas o qué pasaba alrededor?", style = textoNeuro(13), color = Brand.Muted)
                        OutlinedTextField(
                            enabled = !state.guardando && !state.guardadoExitoso,
                            value = state.contexto,
                            onValueChange = onCambioContexto,
                            textStyle = textoNeuro(16),
                            minLines = 2,
                            placeholder = { Text("Ej. Habitación con luz tenue y audífonos", style = textoNeuro(14), color = Color(0xFF7A9389)) },
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Brand.Field, unfocusedContainerColor = Brand.Field,
                                focusedBorderColor = Brand.Teal, unfocusedBorderColor = Brand.Border,
                                focusedTextColor = Brand.Ink, unfocusedTextColor = Brand.Ink
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoNeuro("5", "Resultado / Efectividad")
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("¿Qué tan útil resultó la estrategia?", style = textoNeuro(14, FontWeight.Medium), color = Brand.Ink)
                            Text(descripcionEfectividad(state.resultadoPercibido), style = textoNeuro(12, FontWeight.SemiBold), color = Brand.Emerald)
                        }
                        Row(
                            Modifier.fillMaxWidth().selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { nivel ->
                                val seleccionada = state.resultadoPercibido == nivel
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (seleccionada) Brand.Emerald else Color(0xFFF5F7F3),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .selectable(
                                            selected = seleccionada,
                                            enabled = !state.guardando && !state.guardadoExitoso,
                                            role = Role.RadioButton,
                                            onClick = { onCambioResultado(nivel) }
                                        )
                                ) {
                                    Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            "$nivel",
                                            style = textoNeuro(18, FontWeight.SemiBold),
                                            color = if (seleccionada) Color.White else Brand.Muted
                                        )
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sin efecto", style = textoNeuro(12), color = Brand.Muted)
                            Text("Muy efectiva", style = textoNeuro(12), color = Brand.Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoNeuro(numero: String, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = NeuroSoft) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Text(numero, style = textoNeuro(12, FontWeight.SemiBold), color = NeuroAccent)
            }
        }
        Text(titulo, style = textoNeuro(17, FontWeight.SemiBold), color = Brand.Ink)
    }
}

@Composable
private fun OpcionEstrategiaNeuro(
    estrategia: CatalogoNeuroEntity,
    seleccionada: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val descripcion = when {
        estrategia.nombreEstrategia.contains("Pausa", ignoreCase = true) ->
            "Reduce estímulos auditivos y visuales por 10 a 15 min para restaurar la capacidad de atención."
        estrategia.nombreEstrategia.contains("Aislamiento", ignoreCase = true) ->
            "Uso de audífonos con cancelación de ruido o tapones para estabilizar el sistema auditivo."
        estrategia.nombreEstrategia.contains("Presión", ignoreCase = true) ->
            "Aplicación de peso firme o mantas propioceptivas para enviar señales de calma al sistema nervioso."
        estrategia.nombreEstrategia.contains("Movimiento", ignoreCase = true) ->
            "Balanceos posturales o estimulación táctil regulada para canalizar hiperactividad o tensión."
        else -> "Técnica guiada para estabilizar el procesamiento sensorial y reducir la fatiga."
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (seleccionada) NeuroSoft else Color.White,
        border = BorderStroke(if (seleccionada) 1.5.dp else 1.dp, if (seleccionada) NeuroAccent else Brand.Border),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFD6E8F6)) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_module_neuro),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Autorregulación",
                    style = textoNeuro(11, FontWeight.SemiBold),
                    color = NeuroAccent
                )
                Text(
                    text = estrategia.nombreEstrategia,
                    style = textoNeuro(15, FontWeight.SemiBold),
                    color = if (seleccionada) NeuroAccent else Brand.Ink
                )
                Text(
                    text = descripcion,
                    style = textoNeuro(12),
                    color = Brand.Muted
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(if (seleccionada) NeuroAccent else Color.White, CircleShape)
                    .border(1.dp, if (seleccionada) NeuroAccent else Color(0xFFCADCD2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (seleccionada) {
                    Icon(
                        painter = painterResource(R.drawable.ic_home_check),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorNeuro(mensaje: String) {
    Text(mensaje, style = textoNeuro(12), color = Brand.Error)
}

private fun descripcionNivel(valor: Int) = when (valor) {
    1 -> "Baja"
    2 -> "Leve"
    3 -> "Moderada"
    4 -> "Alta"
    5 -> "Extrema"
    else -> "Elige nivel"
}

private fun descripcionEfectividad(valor: Int) = when (valor) {
    1 -> "Sin efecto"
    2 -> "Poco útil"
    3 -> "Moderada"
    4 -> "Efectiva"
    5 -> "Muy efectiva"
    else -> "Elige nivel"
}