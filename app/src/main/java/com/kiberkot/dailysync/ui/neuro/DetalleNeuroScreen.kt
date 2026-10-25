package com.kiberkot.dailysync.ui.neuro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleNeuroScreen(
    state: NeuroUiState,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estrategiaNombre = state.catalogoEstrategias.find {
        it.idEstrategia == state.idEstrategiaSeleccionada
    }?.nombreEstrategia ?: "No especificada"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Autorregulación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0288D1), Color(0xFF26C6DA)))),
                contentAlignment = Alignment.Center
            ) {
                Text("🧠", fontSize = 28.sp)
            }

            TarjetaDetalle(titulo = "Tipo de Actividad / Tarea", valor = state.tipoActividad.ifBlank { "Sin información" })
            TarjetaDetalle(titulo = "Fecha y Hora del Registro", valor = state.fechaHora.ifBlank { "Fecha reciente" })
            TarjetaDetalle(titulo = "Nivel de Sobrecarga Sensorial", valor = "${state.nivelSobrecarga} / 5")
            TarjetaDetalle(titulo = "Estrategia Aplicada", valor = estrategiaNombre)
            TarjetaDetalle(titulo = "Contexto / Entorno", valor = state.contexto.ifBlank { "Sin contexto registrado" })
            TarjetaDetalle(titulo = "Efectividad / Resultado", valor = "${state.resultadoPercibido} / 5")

            Button(
                onClick = onEditar,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Registro", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TarjetaDetalle(titulo: String, valor: String) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}