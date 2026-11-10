package com.kiberkot.dailysync.ui.adiccion

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Se presenta tras guardar; sólo abre el marcador ante una acción explícita. */
@Composable
internal fun AvisoApoyoAdiccion(onCerrar: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onCerrar,
        title = { Text("Tu registro se guardó") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Si este momento resulta difícil, puedes hacer una pausa y buscar compañía o apoyo.")
                Text("Puedes consultar la habilidad STOP en el catálogo DBT o recurrir a una acción protectora conocida.")
                Text("DailySync no es un servicio de urgencias. Nadie revisa tus registros en tiempo real.")
                Text("Red de apoyo de prueba: persona de confianza (contacto ficticio).")
                Text("Si necesitas ayuda en Chile, puedes abrir el marcador:")
                listOf("Salud Responde · 600 360 7777" to "6003607777",
                    "SAMU · 131" to "131", "Prevención del suicidio · *4141" to "*4141").forEach { (nombre, numero) ->
                    TextButton(onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", numero, null)))
                        } catch (_: android.content.ActivityNotFoundException) {
                            Toast.makeText(context, "Este dispositivo no tiene marcador telefónico.", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) { Text(nombre) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Continuar") } })
}
