package com.kiberkot.dailysync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.model.RegistroAnimo

// COMPONENTE REUTILIZABLE 1: Campo de texto con validación y mensaje de error integrado
@Composable
fun CampoTextoFormulario(
    valor: String,
    etiqueta: String,
    error: String?,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    lineasMinimas: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        minLines = lineasMinimas,
        modifier = modifier.fillMaxWidth()
    )
}

// COMPONENTE REUTILIZABLE 2: Botón principal de acción con control de habilitación
@Composable
fun BotonPrincipal(
    texto: String,
    habilitado: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(texto)
    }
}

// COMPONENTE REUTILIZABLE 3: Tarjeta para mostrar resúmenes de registro en el Historial/Home
@Composable
fun TarjetaRegistroAnimo(
    registro: RegistroAnimo,
    onVerDetalle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onVerDetalle(registro.id) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = registro.categoriaNombre,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Intensidad: ${registro.nivelIntensidad}/5 • Contexto: ${registro.contexto}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fecha: ${registro.fechaHora}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}