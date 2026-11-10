package com.kiberkot.dailysync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

data class RegistroRapidoConfig(val paso: Int, val onOmitir: () -> Unit)

@Composable
fun ControlesRegistroRapido(config: RegistroRapidoConfig?, habilitado: Boolean) {
    if (config == null) return
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Registro rápido · Paso ${config.paso} de 4", color = Brand.Teal,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("registro_rapido_paso"))
            LinearProgressIndicator(progress = { config.paso / 4f }, modifier = Modifier.fillMaxWidth(),
                color = Brand.Teal, trackColor = Brand.Border)
        }
        TextButton(onClick = config.onOmitir, enabled = habilitado,
            modifier = Modifier.testTag("registro_rapido_omitir")) { Text("Omitir", color = if (habilitado) Brand.Teal else Brand.Muted) }
    }
}

/** Mantiene Omitir disponible aunque falle la carga de un catálogo. */
@Composable
fun PieRegistroRapido(config: RegistroRapidoConfig?) {
    if (config == null) return
    Surface {
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
            ControlesRegistroRapido(config, habilitado = true)
        }
    }
}
