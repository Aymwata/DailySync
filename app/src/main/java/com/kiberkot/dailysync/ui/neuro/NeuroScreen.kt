package com.kiberkot.dailysync.ui.neuro

import androidx.activity.compose.BackHandler
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
import com.kiberkot.dailysync.viewmodel.NeuroUiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    BackHandler(enabled = state.isLoading || state.guardadoExitoso) { }

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
                        text = if (editando) "Editar Neurodesarrollo" else "Neurodesarrollo",
                        fontFamily = DailySyncBrand.Font,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        enabled = !(state.isLoading || state.guardadoExitoso)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.catalogoEstrategias.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DailySyncBrand.Teal)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera temática
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
                            .background(DailySyncBrand.Teal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧠", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Registro de Autorregulación",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DailySyncBrand.Ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Monitorea la sobrecarga sensorial y evalúa tus estrategias.",
                            fontFamily = DailySyncBrand.Font,
                            fontSize = 13.sp,
                            color = DailySyncBrand.Muted
                        )
                    }
                }
            }

            // 1. Tipo de Actividad
            OutlinedTextField(
                enabled = !state.isLoading && !state.guardadoExitoso,
                value = state.tipoActividad,
                onValueChange = onCambioTipoActividad,
                label = {
                    Text(
                        "Tipo de Actividad / Tarea",
                        fontFamily = DailySyncBrand.Font,
                        color = DailySyncBrand.Muted
                    )
                },
                placeholder = {
                    Text(
                        "Ej: Estudio intenso, reunión ruidosa...",
                        fontFamily = DailySyncBrand.Font,
                        color = DailySyncBrand.Muted.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
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

            // 2. Sobrecarga Sensorial
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
                            text = "Sobrecarga Sensorial",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.SemiBold,
                            color = DailySyncBrand.Ink
                        )
                        Text(
                            text = "${state.nivelSobrecarga} / 5",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.Bold,
                            color = DailySyncBrand.Teal
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        enabled = !state.isLoading && !state.guardadoExitoso,
                        value = state.nivelSobrecarga.toFloat(),
                        onValueChange = { onCambioSobrecarga(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = DailySyncBrand.Teal,
                            activeTrackColor = DailySyncBrand.Teal,
                            inactiveTrackColor = DailySyncBrand.Border
                        )
                    )
                }
            }

            // 3. Estrategia Aplicada con Guía Explicativa
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Estrategia Aplicada",
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
                    state.catalogoEstrategias.forEach { est ->
                        val seleccionada = state.idEstrategiaSeleccionada == est.idEstrategia
                        FilterChip(
                            enabled = !state.isLoading && !state.guardadoExitoso,
                            selected = seleccionada,
                            onClick = { onSeleccionarEstrategia(est.idEstrategia) },
                            label = {
                                Text(
                                    text = est.nombreEstrategia,
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

                val estrategiaSeleccionada = state.catalogoEstrategias.find { it.idEstrategia == state.idEstrategiaSeleccionada }
                AnimatedVisibility(
                    visible = estrategiaSeleccionada != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    estrategiaSeleccionada?.let { est ->
                        val descripcion = when {
                            est.nombreEstrategia.contains("Pausa", ignoreCase = true) ->
                                "Pausa Sensorial: Consiste en reducir estímulos auditivos y visuales por 10 a 15 minutos para disminuir el agotamiento cognitivo y restaurar la atención."
                            est.nombreEstrategia.contains("Aislamiento", ignoreCase = true) ->
                                "Aislamiento Acústico: Uso de audífonos con cancelación de ruido o tapones en entornos ruidosos para estabilizar el sistema auditivo."
                            est.nombreEstrategia.contains("Presión", ignoreCase = true) ->
                                "Presión Profunda: Aplicación de peso firme o mantas propioceptivas para enviar señales de calma al sistema nervioso central."
                            est.nombreEstrategia.contains("Movimiento", ignoreCase = true) ->
                                "Movimiento Rítmico (Stimming): Balanceos posturales o estimulación táctil regulada para canalizar el exceso de hiperactividad o ansiedad."
                            else -> "Técnica de autorregulación guiada para estabilizar el procesamiento sensorial y reducir el estrés."
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
                                        text = "💡 ¿Cómo aplicar esta estrategia?",
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

            // 4. Contexto / Entorno
            OutlinedTextField(
                enabled = !state.isLoading && !state.guardadoExitoso,
                value = state.contexto,
                onValueChange = onCambioContexto,
                label = {
                    Text(
                        "Contexto / Entorno (Opcional)",
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

            // 5. Resultado / Efectividad
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
                            text = "Resultado / Efectividad",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.SemiBold,
                            color = DailySyncBrand.Ink
                        )
                        Text(
                            text = "${state.resultadoPercibido} / 5",
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.Bold,
                            color = DailySyncBrand.Emerald
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        enabled = !state.isLoading && !state.guardadoExitoso,
                        value = state.resultadoPercibido.toFloat(),
                        onValueChange = { onCambioResultado(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = DailySyncBrand.Emerald,
                            activeTrackColor = DailySyncBrand.Emerald,
                            inactiveTrackColor = DailySyncBrand.Border
                        )
                    )
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