package com.kiberkot.dailysync.ui.registro

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
import com.kiberkot.dailysync.viewmodel.AnimoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAnimoScreen(
    state: AnimoUiState,
    registroId: String,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val registro = state.historial.find { it.id == registroId }
    val emojiMap = mapOf(1 to "🧘", 2 to "🚀", 3 to "⚡", 4 to "🌧️", 5 to "💔")
    val emoji = emojiMap[registro?.categoriaId] ?: "😊"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Estado de Ánimo", fontWeight = FontWeight.Bold) },
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
                    .background(Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }

            TarjetaDetalle(
                titulo = "Estado Emocional",
                valor = registro?.categoriaNombre ?: "Cargando..."
            )
            TarjetaDetalle(
                titulo = "Fecha y Hora del Registro",
                valor = registro?.fechaHora ?: "Reciente"
            )
            TarjetaDetalle(
                titulo = "Nivel de Intensidad",
                valor = "${registro?.nivelIntensidad ?: 3} / 5"
            )
            TarjetaDetalle(
                titulo = "Contexto",
                valor = registro?.contexto ?: "Sin contexto registrado"
            )
            TarjetaDetalle(
                titulo = "Nota Opcional",
                valor = registro?.notaOpcional?.ifBlank { "Sin nota" } ?: "Sin nota"
            )

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