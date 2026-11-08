package com.kiberkot.dailysync.ui.adiccion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.ui.components.AvisoRegistroGuardado
import com.kiberkot.dailysync.ui.theme.DailySyncBrand
import com.kiberkot.dailysync.viewmodel.AdiccionUiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdiccionScreen(
    state: AdiccionUiState,
    editando: Boolean = false,
    onCambioImpulso: (Int) -> Unit,
    onCambioDesencadenante: (String) -> Unit,
    onSeleccionarAccion: (Int) -> Unit,
    onCambioEstadoObjetivo: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val opcionesObjetivo = listOf("Logrado", "En proceso", "Resistido")

    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = DailySyncBrand.Background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DailySyncBrand.Background,
                    titleContentColor = DailySyncBrand.Ink,
                    navigationIconContentColor = DailySyncBrand.Ink
                ),
                title = {
                    Text(
                        text = if (editando) "Editar Craving" else "Adicciones y Craving",
                        fontFamily = DailySyncBrand.Font,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = DailySyncBrand.Field,
                border = BorderStroke(1.dp, DailySyncBrand.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DailySyncBrand.Error.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Registro de Craving",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DailySyncBrand.Ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Monitorea impulsos y aplica acciones protectoras.",
                            fontFamily = DailySyncBrand.Font,
                            fontSize = 13.sp,
                            color = DailySyncBrand.Muted
                        )
                    }
                }
            }

            // Nivel de Impulso
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DailySyncBrand.Field,
                border = BorderStroke(1.dp, DailySyncBrand.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nivel de Impulso / Craving",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.SemiBold,
                            color = DailySyncBrand.Ink
                        )
                        Text(
                            text = "${state.nivelImpulso} / 10",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.Bold,
                            color = DailySyncBrand.Error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = state.nivelImpulso.toFloat(),
                        onValueChange = { onCambioImpulso(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = DailySyncBrand.Error,
                            activeTrackColor = DailySyncBrand.Error,
                            inactiveTrackColor = DailySyncBrand.Border
                        )
                    )
                }
            }

            // Desencadenante
            OutlinedTextField(
                value = state.desencadenante,
                onValueChange = onCambioDesencadenante,
                label = {
                    Text(
                        "Desencadenante / Gatillo",
                        fontFamily = DailySyncBrand.Font,
                        color = DailySyncBrand.Muted
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DailySyncBrand.Field,
                    unfocusedContainerColor = DailySyncBrand.Field,
                    focusedBorderColor = DailySyncBrand.Teal,
                    unfocusedBorderColor = DailySyncBrand.Border,
                    focusedTextColor = DailySyncBrand.Ink,
                    unfocusedTextColor = DailySyncBrand.Ink
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Acción Protectora + Guía Explicativa
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Acción Protectora",
                    fontFamily = DailySyncBrand.Font,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DailySyncBrand.Ink,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.catalogoAcciones.forEach { acc ->
                        val seleccionada = state.idAccionSeleccionada == acc.idAccionProtectora
                        FilterChip(
                            selected = seleccionada,
                            onClick = { onSeleccionarAccion(acc.idAccionProtectora) },
                            label = {
                                Text(
                                    text = acc.nombreAccion,
                                    fontFamily = DailySyncBrand.Font,
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DailySyncBrand.Teal,
                                selectedLabelColor = Color.White,
                                containerColor = DailySyncBrand.Field,
                                labelColor = DailySyncBrand.Ink
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = seleccionada,
                                borderColor = DailySyncBrand.Border,
                                selectedBorderColor = DailySyncBrand.Teal
                            )
                        )
                    }
                }

                val accionSeleccionada = state.catalogoAcciones.find { it.idAccionProtectora == state.idAccionSeleccionada }
                AnimatedVisibility(
                    visible = accionSeleccionada != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    accionSeleccionada?.let { acc ->
                        val descripcion = when {
                            acc.nombreAccion.contains("Surf", ignoreCase = true) || acc.nombreAccion.contains("Ola", ignoreCase = true) ->
                                "Surfear la Ola (Urge Surfing): Observa la necesidad sin actuar durante 15 minutos. Comprende que el impulso sube a una cima y luego desciende naturalmente."
                            acc.nombreAccion.contains("Entorno", ignoreCase = true) || acc.nombreAccion.contains("Cambio", ignoreCase = true) ->
                                "Cambio de Entorno: Abandona el lugar físico o apaga el estímulo disparador inmediatamente para romper el bucle de la compulsión."
                            acc.nombreAccion.contains("Soporte", ignoreCase = true) || acc.nombreAccion.contains("Contacto", ignoreCase = true) ->
                                "Contacto de Soporte: Llama o escribe a una persona de confianza declarando el impulso abiertamente para diluir la urgencia emocional."
                            acc.nombreAccion.contains("Sensorial", ignoreCase = true) || acc.nombreAccion.contains("Contraste", ignoreCase = true) ->
                                "Estímulo Sensorial de Contraste: Utiliza agua fría en el rostro o hielo en las manos para activar la respuesta vagal y frenar el craving."
                            else -> "Acción preventiva de contención diseñada para proteger tu proceso de autocontrol y evitar la recaída."
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DailySyncBrand.Teal.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, DailySyncBrand.Teal.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = DailySyncBrand.Teal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "🛡️ Guía de la acción protectora",
                                        fontFamily = DailySyncBrand.Font,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DailySyncBrand.Ink
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = descripcion,
                                        fontFamily = DailySyncBrand.Font,
                                        fontSize = 13.sp,
                                        color = DailySyncBrand.Ink.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cumplimiento de Objetivo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cumplimiento de Objetivo",
                    fontFamily = DailySyncBrand.Font,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DailySyncBrand.Ink,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    opcionesObjetivo.forEach { obj ->
                        val seleccionado = state.estadoObjetivo == obj
                        FilterChip(
                            selected = seleccionado,
                            onClick = { onCambioEstadoObjetivo(obj) },
                            label = {
                                Text(
                                    text = obj,
                                    fontFamily = DailySyncBrand.Font,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DailySyncBrand.Emerald,
                                selectedLabelColor = Color.White,
                                containerColor = DailySyncBrand.Field,
                                labelColor = DailySyncBrand.Ink
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = seleccionado,
                                borderColor = DailySyncBrand.Border,
                                selectedBorderColor = DailySyncBrand.Emerald
                            )
                        )
                    }
                }
            }

            state.errorMsg?.let {
                Text(
                    text = it,
                    fontFamily = DailySyncBrand.Font,
                    color = DailySyncBrand.Error,
                    fontSize = 13.sp
                )
            }

            AvisoRegistroGuardado(
                visible = state.guardadoExitoso, editando = editando
            )

            Button(
                onClick = onGuardar,
                enabled = !state.isLoading && !state.guardadoExitoso,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DailySyncBrand.Teal,
                    contentColor = Color.White,
                    disabledContainerColor = DailySyncBrand.Border,
                    disabledContentColor = DailySyncBrand.Muted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (editando) "Guardar Cambios" else "Guardar Registro",
                        fontFamily = DailySyncBrand.Font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}