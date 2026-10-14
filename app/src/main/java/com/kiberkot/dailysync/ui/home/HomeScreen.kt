package com.kiberkot.dailysync.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.ui.components.TarjetaHeroDashboard
import com.kiberkot.dailysync.ui.components.TarjetaRegistroAnimoPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    historial: List<RegistroAnimo>,
    onNuevoRegistro: () -> Unit,
    onVerDetalle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bienvenido de nuevo 👋",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = "DailySync",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoRegistro,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Check-in", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            val promedioStr = if (historial.isNotEmpty()) {
                String.format("%.1f", historial.map { it.nivelIntensidad }.average())
            } else "0.0"

            TarjetaHeroDashboard(
                totalRegistros = historial.size,
                promedio = promedioStr,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Historial Reciente",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (historial.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🌿", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin registros aún en Room DB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toca '+ Nuevo Check-in' para probar el flujo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(historial, key = { it.id }) { registro ->
                        TarjetaRegistroAnimoPro(
                            registro = registro,
                            onVerDetalle = onVerDetalle,
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
        }
    }
}