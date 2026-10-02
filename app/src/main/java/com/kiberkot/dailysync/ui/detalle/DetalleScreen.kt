package com.kiberkot.dailysync.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.model.RegistroAnimo

@Composable
fun DetalleScreen(
    registro: RegistroAnimo?,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Detalle del Registro",
            style = MaterialTheme.typography.headlineSmall
        )

        if (registro == null) {
            Text(
                text = "No se encontró el registro seleccionado.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Estado: ${registro.categoriaNombre}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = "Nivel de Intensidad: ${registro.nivelIntensidad} de 5")
                    Text(text = "Contexto: ${registro.contexto}")
                    Text(text = "Fecha y hora: ${registro.fechaHora}")
                    Text(text = "ID de sincronización: ${registro.id}")

                    if (!registro.notaOpcional.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nota personal:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = registro.notaOpcional,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}