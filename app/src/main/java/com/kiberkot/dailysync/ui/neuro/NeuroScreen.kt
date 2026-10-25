package com.kiberkot.dailysync.ui.neuro

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
import com.kiberkot.dailysync.viewmodel.NeuroUiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NeuroScreen(
    state: NeuroUiState,
    onCambioTipoActividad: (String) -> Unit,
    onCambioSobrecarga: (Int) -> Unit,
    onSeleccionarEstrategia: (Int) -> Unit,
    onCambioContexto: (String) -> Unit,
    onCambioResultado: (Int) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neurodesarrollo", fontWeight = FontWeight.Bold) },
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
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0288D1), Color(0xFF26C6DA)))),
                contentAlignment = Alignment.Center
            ) {
                Text("🧠", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Registro de Autorregulación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.tipoActividad,
                onValueChange = onCambioTipoActividad,
                label = { Text("Tipo de Actividad / Tarea") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sobrecarga Sensorial: ${state.nivelSobrecarga} / 5", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.nivelSobrecarga.toFloat(),
                        onValueChange = { onCambioSobrecarga(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Estrategia Aplicada", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.catalogoEstrategias.forEach { est ->
                    FilterChip(
                        selected = state.idEstrategiaSeleccionada == est.idEstrategia,
                        onClick = { onSeleccionarEstrategia(est.idEstrategia) },
                        label = { Text(est.nombreEstrategia) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.contexto,
                onValueChange = onCambioContexto,
                label = { Text("Contexto / Entorno (Opcional)") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resultado / Efectividad: ${state.resultadoPercibido} / 5", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.resultadoPercibido.toFloat(),
                        onValueChange = { onCambioResultado(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3
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
                else Text("Guardar Registro", fontWeight = FontWeight.Bold)
            }
        }
    }
}