package com.kiberkot.dailysync.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.components.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.viewmodel.ConsentimientoUiState
import com.kiberkot.dailysync.viewmodel.VERSION_CONSENTIMIENTO
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConsentimientoScreen(
    state: ConsentimientoUiState,
    onAceptar: () -> Unit,
    onSalir: () -> Unit,
    onReintentar: () -> Unit,
    soloLectura: Boolean = false
) {
    var confirmado by rememberSaveable { mutableStateOf(false) }
    BackHandler { if (!state.guardando) onSalir() }
    Scaffold(containerColor = FondoPantallaMarca, topBar = {
        CabeceraMarca(if (soloLectura) "Mis datos" else "Antes de comenzar", "DailySync", onSalir,
            R.drawable.ic_dailysync_logo, ocupado = state.guardando, tinteIcono = null)
    }, bottomBar = {
        PieMarca {
                if (soloLectura) {
                    AccionMarca("Volver", onSalir, icono = R.drawable.ic_form_back)
                } else {
                    AccionMarca(if (state.guardando) "Guardando…" else "Aceptar y continuar", onAceptar,
                        habilitado = confirmado && !state.cargando && state.error == null,
                        guardando = state.guardando, modifier = Modifier.testTag("consentimiento_aceptar"))
                    TextButton(onClick = onSalir, enabled = !state.guardando,
                        modifier = Modifier.fillMaxWidth()) { Text("Ahora no · Cerrar sesión", style = textoMarca(14, FontWeight.Medium), color = Brand.Muted) }
                }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "introduccion") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Consentimiento de uso", style = textoMarca(25, FontWeight.Bold), color = Brand.Ink)
                Text("DailySync · Prototipo académico · Versión $VERSION_CONSENTIMIENTO", style = textoMarca(13), color = Brand.Muted)
            }
            }
            item(key = "uso") { ConsentimientoParrafo("Para qué sirve",
                "DailySync reúne herramientas de autorregistro de ánimo, habilidades DBT, neurodesarrollo y adicciones. Esta versión está en desarrollo y se utiliza únicamente como demostración académica.") }
            item(key = "datos_ficticios") { ConsentimientoParrafo("Utiliza sólo información ficticia",
                "Prueba los formularios con situaciones y cuentas inventadas. No ingreses datos personales reales, información real de salud ni datos de otras personas.") }
            item(key = "almacenamiento") { ConsentimientoParrafo("Qué se guarda",
                "Los registros y los datos de la cuenta de prueba se guardan localmente en este dispositivo. También se guarda, para esta cuenta, la versión de este consentimiento y la fecha de aceptación. Esta versión no sincroniza los registros con un servidor.") }
            item(key = "limites") { ConsentimientoParrafo("Alcance y límites",
                "La aplicación no entrega diagnósticos ni tratamientos y no reemplaza la atención profesional. No es un servicio de urgencias: nadie revisa los registros en tiempo real. Su seguridad sigue en desarrollo; no está preparada para utilizar datos reales ni para uso en producción.") }
            item(key = "decision") { ConsentimientoParrafo("Tu decisión",
                "Participar es voluntario. Puedes cerrar sesión sin aceptar. Para acceder a las funciones de registro debes aceptar este consentimiento. Puedes volver a leerlo en Perfil → Mis datos, y consultar, corregir o eliminar tus registros desde el historial.") }
            state.aceptacion?.let { aceptacion ->
                item(key = "aceptacion") {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm z", Locale("es", "CL"))
                    .format(Date(aceptacion.fechaUtc))
                TarjetaMarca(fondo = FondoSuaveMarca) {
                    Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(24.dp), tint = Brand.Teal)
                    Text("Aceptación guardada · Versión ${aceptacion.version}\n$fecha", style = textoMarca(14, FontWeight.Medium),
                        color = Brand.Teal, modifier = Modifier.testTag("consentimiento_fecha"))
                }
                }
            }
            if (state.cargando) item(key = "cargando") { CircularProgressIndicator(color = Brand.Teal) }
            state.error?.let {
                item(key = "error") {
                Column {
                Text(it, style = textoMarca(13), color = Brand.Error)
                TextButton(onClick = onReintentar, enabled = !state.guardando) { Text("Reintentar", style = textoMarca(14, FontWeight.Medium), color = Brand.Teal) }
                }
                }
            }
            if (!soloLectura && !state.cargando && state.error == null) {
                item(key = "confirmar") {
                Surface(shape = RoundedCornerShape(20.dp), color = if (confirmado) FondoSuaveMarca else Color.White,
                    border = BorderStroke(1.dp, if (confirmado) Brand.Teal else Brand.Border)) {
                    Row(Modifier.fillMaxWidth().toggleable(value = confirmado, enabled = !state.guardando,
                        role = Role.Checkbox, onValueChange = { confirmado = it })
                        .testTag("consentimiento_confirmar").padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = confirmado, onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = Brand.Teal, uncheckedColor = Brand.Muted, checkmarkColor = Color.White))
                        Spacer(Modifier.width(8.dp))
                        Text("He leído y acepto estas condiciones para usar el prototipo con datos ficticios.",
                            style = textoMarca(14), color = Brand.Ink)
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun ConsentimientoParrafo(titulo: String, texto: String) {
    TarjetaMarca {
        Text(titulo, style = textoMarca(17, FontWeight.SemiBold), color = Brand.Ink)
        Text(texto, style = textoMarca(14), color = Brand.Muted)
    }
}
