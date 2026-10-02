package com.kiberkot.dailysync.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.ui.components.TarjetaRegistroAnimo

@Composable
fun HomeScreen(
    historial: List<RegistroAnimo>,
    onNuevoRegistro: () -> Unit,
    onVerDetalle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoRegistro) {
                Text(text = "+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { innerPadding ->
        if (historial.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay registros aún.\nToca '+' para hacer tu primer check-in.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Historial DailySync",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                items(historial, key = { it.id }) { registro ->
                    TarjetaRegistroAnimo(
                        registro = registro,
                        onVerDetalle = onVerDetalle
                    )
                }
            }
        }
    }
}