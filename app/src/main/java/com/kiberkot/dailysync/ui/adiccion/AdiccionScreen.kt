package com.kiberkot.dailysync.ui.adiccion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        topBar = {
            TopAppBar(
                title = { Text(if (editando) "Editar Craving" else "Adicciones y Craving", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFFF7043)))),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Registro de Craving", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nivel de Impulso / Craving: ${state.nivelImpulso} / 10", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.nivelImpulso.toFloat(),
                        onValueChange = { onCambioImpulso(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.desencadenante,
                onValueChange = onCambioDesencadenante,
                label = { Text("Desencadenante / Gatillo") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Acción Protectora", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.catalogoAcciones.forEach { acc ->
                    FilterChip(
                        selected = state.idAccionSeleccionada == acc.idAccionProtectora,
                        onClick = { onSeleccionarAccion(acc.idAccionProtectora) },
                        label = { Text(acc.nombreAccion) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Cumplimiento de Objetivo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                opcionesObjetivo.forEach { obj ->
                    FilterChip(
                        selected = state.estadoObjetivo == obj,
                        onClick = { onCambioEstadoObjetivo(obj) },
                        label = { Text(obj) }
                    )
                }
            }

            state.errorMsg?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGuardar,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(color = Color.White)
                else Text(if (editando) "Guardar Cambios" else "Guardar Registro", fontWeight = FontWeight.Bold)
            }
        }
    }
    }