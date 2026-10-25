package com.kiberkot.dailysync.ui.dbt

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
import com.kiberkot.dailysync.viewmodel.RegistroDbtUiState

@OptIn(ExperimentalMaterial3Api::class)
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
    val habilidadNombre = state.registro?.habilidadNombre
        ?: state.catalogo.find { it.id == state.formulario.habilidadId }?.nombre
        ?: "Práctica DBT"

    val situacionTexto = state.registro?.situacion
        ?: state.formulario.situacion

    val intensidadValor = state.registro?.intensidad
        ?: state.formulario.intensidad

    val utilidadValor = state.registro?.utilidad
        ?: state.formulario.utilidad

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Práctica DBT", fontWeight = FontWeight.Bold) },
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
        if (state.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7E57C2), Color(0xFFAB47BC)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💬", fontSize = 28.sp)
                }

                TarjetaDetalle(titulo = "Habilidad DBT", valor = habilidadNombre)
                TarjetaDetalle(titulo = "Situación asociada", valor = situacionTexto.ifBlank { "Sin especificación" })
                TarjetaDetalle(titulo = "Intensidad Emocional", valor = "$intensidadValor / 5")
                TarjetaDetalle(titulo = "Utilidad Percibida", valor = "$utilidadValor / 5")

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