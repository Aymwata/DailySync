package com.kiberkot.dailysync.ui.notificaciones

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.components.*
import android.view.ContextThemeWrapper
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.viewmodel.NotificacionesUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(state: NotificacionesUiState, onCambioActivo: (Boolean) -> Unit,
    onCambioHora: (String) -> Unit, onActualizarPermisos: () -> Unit,
    onReintentar: () -> Unit, onVolver: () -> Unit) {
    val context = LocalContext.current
    val actividad = LocalActividadSesion.current
    var selectorHora by remember { mutableStateOf<TimePickerDialog?>(null) }
    DisposableEffect(Unit) { onDispose { selectorHora?.dismiss() } }
    val ciclo = LocalLifecycleOwner.current
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        onActualizarPermisos()
        if (concedido && actividad()) onCambioActivo(true)
    }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) onActualizarPermisos()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }
    BackHandler(enabled = state.guardando) { }
    val habilitado = !state.cargando && !state.guardando
    Scaffold(containerColor = FondoPantallaMarca, topBar = {
        CabeceraMarca("Notificaciones", "Tu perfil", onVolver, R.drawable.ic_notifications, ocupado = state.guardando)
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Tu recordatorio diario", style = textoMarca(25, FontWeight.Bold), color = Brand.Ink)
            Text("Recibe un aviso si todavía no has guardado ningún registro durante el día.",
                style = textoMarca(14), color = Brand.Muted)
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, Brand.Border)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Recordatorio diario", style = textoMarca(16, FontWeight.SemiBold), color = Brand.Ink)
                        Text(if (state.activo) "Activado" else "Desactivado", style = textoMarca(14), color = Brand.Muted)
                    }
                    Switch(checked = state.activo, enabled = habilitado,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Brand.Teal,
                            checkedBorderColor = Brand.Teal, uncheckedTrackColor = Brand.Field,
                            uncheckedThumbColor = Brand.Muted, uncheckedBorderColor = Brand.Border), onCheckedChange = { activo ->
                        if (actividad()) {
                            if (activo && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else onCambioActivo(activo)
                        }
                    }, modifier = Modifier.testTag("recordatorio_switch").semantics { contentDescription = "Activar recordatorio diario" })
                }
            }
            Surface(onClick = {
                if (actividad()) {
                    val partes = state.hora.split(":")
                    selectorHora = TimePickerDialog(ContextThemeWrapper(context, R.style.DialogoHoraMarca), { _, hora, minuto ->
                        if (actividad()) onCambioHora(String.format(Locale.ROOT, "%02d:%02d", hora, minuto))
                    }, partes[0].toInt(), partes[1].toInt(), true).also { it.show() }
                }
            }, enabled = habilitado, shape = RoundedCornerShape(24.dp), color = Color.White,
                border = BorderStroke(1.dp, Brand.Border), modifier = Modifier.testTag("recordatorio_hora")) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Hora del aviso", style = textoMarca(16, FontWeight.SemiBold), color = Brand.Ink)
                        Text("Todos los días", style = textoMarca(14), color = Brand.Muted)
                    }
                    Text(state.hora, style = textoMarca(25, FontWeight.SemiBold), color = Brand.Teal)
                }
            }
            if (state.permisosCargados && !state.permitidas) Surface(shape = RoundedCornerShape(20.dp), color = Brand.Field) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Permiso de notificaciones", style = textoMarca(16, FontWeight.SemiBold), color = Brand.Ink)
                    Text("Android está bloqueando los avisos de DailySync. Permítelos para recibir el recordatorio.",
                        style = textoMarca(14), color = Brand.Muted)
                    TextButton(onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        context.startActivity(intent)
                    }) { Text("Abrir ajustes de Android", style = textoMarca(14, FontWeight.Medium), color = Brand.Teal) }
                }
            }
            if (state.cargando || state.guardando) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Brand.Teal)
            state.error?.let { error ->
                Text(error, color = Brand.Error, style = textoMarca(13),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                TextButton(onClick = onReintentar, enabled = !state.guardando) { Text("Reintentar", style = textoMarca(14, FontWeight.Medium), color = Brand.Teal) }
            }
            TarjetaMarca {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp), tint = Brand.Teal)
                    Text("Cuándo recibirás el aviso", style = textoMarca(17, FontWeight.SemiBold), color = Brand.Ink)
                }
                Text("Cualquier registro de ánimo, DBT, neurodesarrollo o adicciones de hoy evita el aviso. Se envía como máximo una vez al día.",
                    style = textoMarca(14), color = Brand.Muted)
                Text("Si la hora elegida ya pasó al activarlo, se comprobará el día de hoy. La entrega puede retrasarse por el ahorro de batería del teléfono.",
                    style = textoMarca(13), color = Brand.Muted)
            }
            TarjetaMarca(fondo = FondoSuaveMarca) {
                Text("Aviso discreto", style = textoMarca(17, FontWeight.SemiBold), color = Brand.Ink)
                Text("La notificación no muestra ámbitos, valores ni datos de tu cuenta. Al tocarla se abre DailySync; si tu sesión venció, deberás iniciar sesión.",
                    style = textoMarca(14), color = Brand.Muted)
            }
        }
    }
}
