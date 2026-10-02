package com.kiberkot.dailysync.ui.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.ui.components.BotonPrincipal
import com.kiberkot.dailysync.ui.components.CampoTextoFormulario
import com.kiberkot.dailysync.viewmodel.AnimoUiState

@Composable
fun RegistroAnimoScreen(
    state: AnimoUiState,
    onSeleccionarCategoria: (com.kiberkot.dailysync.model.CategoriaAnimo) -> Unit,
    onCambioIntensidad: (Float) -> Unit,
    onCambioContexto: (String) -> Unit,
    onCambioNota: (String) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Check-in de Estado de Ánimo",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Registra tu percepción emocional actual (RF12)",
            style = MaterialTheme.typography.bodyMedium
        )

        // Selector de Categoría (RadioButtons con catálogo sintético)
        Text(
            text = "1. ¿Cómo te sientes en este momento?",
            style = MaterialTheme.typography.titleMedium
        )
        state.categorias.forEach { categoria ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = state.categoriaSeleccionada?.id == categoria.id,
                    onClick = { onSeleccionarCategoria(categoria) }
                )
                Text(text = categoria.nombre)
            }
        }
        if (state.errorCategoria != null) {
            Text(
                text = state.errorCategoria,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Selector de Intensidad (Slider)
        Text(
            text = "2. Nivel de intensidad: ${state.nivelIntensidad.toInt()} / 5",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = state.nivelIntensidad,
            onValueChange = onCambioIntensidad,
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Componente Reutilizable 1: Campo de Contexto
        CampoTextoFormulario(
            valor = state.contexto,
            etiqueta = "3. Contexto o entorno (Ej: Hogar, Trabajo, Estudio)",
            error = state.errorContexto,
            onValorChange = onCambioContexto
        )

        // Componente Reutilizable 1: Campo de Nota Opcional
        CampoTextoFormulario(
            valor = state.notaOpcional,
            etiqueta = "4. Nota personal opcional",
            error = null,
            onValorChange = onCambioNota,
            lineasMinimas = 3
        )

        // Componente Reutilizable 2: Botón Principal
        BotonPrincipal(
            texto = "Guardar Check-in",
            onClick = onGuardar
        )

        // Retroalimentación de Éxito
        state.mensajeResultado?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}