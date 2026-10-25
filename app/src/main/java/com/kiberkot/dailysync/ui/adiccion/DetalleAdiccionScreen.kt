package com.kiberkot.dailysync.ui.adiccion

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
import com.kiberkot.dailysync.viewmodel.AdiccionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAdiccionScreen(
    state: AdiccionUiState,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accionNombre = state.catalogoAcciones.find {
        it.idAccionProtectora == state.idAccionSeleccionada
    }?.nombreAccion ?: "No especificada"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Craving", fontWeight = FontWeight.Bold) },
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
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFFF7043)))),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", fontSize = 28.sp)
            }

            TarjetaDetalle(titulo = "Nivel de Impulso / Craving", valor = "${state.nivelImpulso} / 10")
            TarjetaDetalle(titulo = "Desencadenante / Gatillo", valor = state.desencadenante.ifBlank { "Sin especificar" })
            TarjetaDetalle(titulo = "Fecha y Hora del Registro", valor = state.fechaHora.ifBlank { "Fecha reciente" })
            TarjetaDetalle(titulo = "Acción Protectora Aplicada", valor = accionNombre)
            TarjetaDetalle(titulo = "Cumplimiento de Objetivo", valor = state.estadoObjetivo)

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
                color = MaterialTheme.colorScheme.error
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