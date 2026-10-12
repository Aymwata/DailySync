package com.kiberkot.dailysync.ui.registro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.ui.componentes.CategoriaChipGrid
import com.kiberkot.dailysync.viewmodel.AnimoUiState

@Composable
fun RegistroAnimoScreen(
    state: AnimoUiState,
    onSeleccionarCategoria: (CategoriaAnimo) -> Unit,
    onCambioIntensidad: (Float) -> Unit,
    onCambioContexto: (String) -> Unit,
    onCambioNota: (String) -> Unit,
    onGuardar: () -> Unit,
    onOcultarBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("🧘", "🚀", "⚡", "🌧️", "💔")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Nuevo Check-in",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        AnimatedVisibility(
            visible = state.bannerExitoVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("¡Guardado en Room DB!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onOcultarBanner) {
                        Text("Cerrar")
                    }
                }
            }
        }

        Text(
            text = "1. Selecciona tu estado emocional",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.categorias.forEachIndexed { index, cat ->
                CategoriaChipGrid(
                    categoria = cat,
                    emoji = emojis.getOrElse(index) { "😊" },
                    seleccionada = state.categoriaSeleccionada?.id == cat.id,
                    onSelect = { onSeleccionarCategoria(cat) }
                )
            }
        }

        if (state.errorCategoria != null) {
            Text(
                text = state.errorCategoria,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Intensidad",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${state.nivelIntensidad.toInt()} / 5",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = state.nivelIntensidad,
                    onValueChange = onCambioIntensidad,
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Text(
            text = "3. Contexto del registro",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        OutlinedTextField(
            value = state.contexto,
            onValueChange = onCambioContexto,
            label = { Text("¿Qué estás haciendo? (Mínimo 3 letras)") },
            isError = state.errorContexto != null,
            supportingText = {
                state.errorContexto?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.notaOpcional,
            onValueChange = onCambioNota,
            label = { Text("Nota opcional") },
            minLines = 2,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onGuardar,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Guardar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}