package com.kiberkot.dailysync.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.components.DailySyncLogo
import com.kiberkot.dailysync.ui.theme.BarrasClarasMarca
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.ui.theme.DailySyncTheme
import com.kiberkot.dailysync.viewmodel.ActividadDiaUi
import com.kiberkot.dailysync.viewmodel.HomeUiState
import com.kiberkot.dailysync.viewmodel.ModuloHome
import com.kiberkot.dailysync.viewmodel.SeccionHome

private data class PaletaModulo(val fondo: Color, val acento: Color, @DrawableRes val icono: Int)

private val paletasModulo = mapOf(
    ModuloHome.ANIMO to PaletaModulo(Color(0xFFFFF4DC), Color(0xFF94631D), R.drawable.ic_module_animo),
    ModuloHome.DBT to PaletaModulo(Color(0xFFF0EBF9), Color(0xFF756096), R.drawable.ic_module_dbt),
    ModuloHome.NEURODESARROLLO to PaletaModulo(Color(0xFFE8F2FA), Color(0xFF466F97), R.drawable.ic_module_neuro),
    ModuloHome.ADICCIONES to PaletaModulo(Color(0xFFFBECE6), Color(0xFFA56150), R.drawable.ic_module_adiccion)
)

private fun paleta(modulo: ModuloHome) = paletasModulo.getValue(modulo)

private fun estiloHome(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Brand.Font, fontSize = size.sp, lineHeight = (size * 1.4f).sp,
    fontWeight = weight, letterSpacing = (-0.2).sp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAbrirModulo: (ModuloHome) -> Unit,
    onAbrirSeccion: (SeccionHome) -> Unit,
    onReintentar: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mostrarSelector by rememberSaveable { mutableStateOf(false) }
    BarrasClarasMarca()
    Box(modifier.fillMaxSize().background(Brush.verticalGradient(
        listOf(Color(0xFFF0F9F5), Color(0xFFF8FAF8))))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { CabeceraHome(state.iniciales, onCerrarSesion) },
            bottomBar = { BarraInferiorHome(onAbrirSeccion) }
        ) { innerPadding ->
            // Son pocas secciones fijas: mantenerlas compuestas evita recrear
            // textos, vectores y tarjetas cada vez que vuelven a entrar en pantalla.
            Column(
                Modifier.fillMaxSize().padding(innerPadding)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(state.fecha, style = estiloHome(14), color = Brand.Muted)
                    Text(state.saludo, style = estiloHome(28, FontWeight.Bold), color = Brand.Ink)
                }

                if (state.error != null) {
                    Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Text(state.error, style = estiloHome(14), color = Brand.Error)
                            TextButton(onClick = onReintentar) {
                                Text("Reintentar", style = estiloHome(14, FontWeight.SemiBold), color = Brand.Teal)
                            }
                        }
                    }
                } else ActividadSemanalCard(state.actividadSemanal)

                val shape = RoundedCornerShape(20.dp)
                Button(
                    onClick = { mostrarSelector = true }, shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .shadow(8.dp, shape, ambientColor = Brand.Teal.copy(alpha = 0.12f),
                            spotColor = Brand.Teal.copy(alpha = 0.18f))
                        .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), shape)
                ) {
                    Icon(painterResource(R.drawable.ic_home_plus), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Nuevo registro", style = estiloHome(16, FontWeight.SemiBold))
                }

                Text("Ámbitos de autorreporte", style = estiloHome(17, FontWeight.SemiBold), color = Brand.Ink)

                repeat(2) { fila ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloHome.entries.drop(fila * 2).take(2).forEach { modulo ->
                            TarjetaModulo(modulo, { onAbrirModulo(modulo) }, Modifier.weight(1f))
                        }
                    }
                }
                Text("Proyecto académico · Utiliza datos ficticios.", style = estiloHome(12), color = Brand.Muted)

            }
        }
        if (mostrarSelector) {
            ModalBottomSheet(
                onDismissRequest = { mostrarSelector = false },
                containerColor = Color(0xFFF8FBF9), contentColor = Brand.Ink,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFC7DAD1)) }
            ) {
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("¿Qué quieres registrar?", style = estiloHome(22, FontWeight.SemiBold), color = Brand.Ink)
                    Text("Elige el espacio que necesitas hoy.", style = estiloHome(14), color = Brand.Muted)
                    Spacer(Modifier.height(2.dp))
                    ModuloHome.entries.forEach { modulo ->
                        val colores = paleta(modulo)
                        Surface(onClick = {
                            mostrarSelector = false
                            onAbrirModulo(modulo)
                        }, shape = RoundedCornerShape(20.dp), color = colores.fondo,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp)) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Image(painterResource(colores.icono), contentDescription = null, modifier = Modifier.size(42.dp))
                                Text(modulo.titulo, style = estiloHome(15, FontWeight.SemiBold), color = colores.acento,
                                    modifier = Modifier.weight(1f))
                                Icon(painterResource(R.drawable.ic_home_arrow), contentDescription = null,
                                    tint = colores.acento, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraHome(iniciales: String, onCerrarSesion: () -> Unit) {
    var mostrarPerfil by remember { mutableStateOf(false) }
    Surface(color = Color(0xFFF5FBF8)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DailySyncLogo(Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text("DailySync", style = estiloHome(20, FontWeight.Bold), color = Brand.Ink)
                Text("Tu espacio diario", style = estiloHome(12), color = Brand.Muted)
            }
            Surface(shape = RoundedCornerShape(50), color = Color(0xFFE5F3EB),
                modifier = Modifier.clearAndSetSemantics { contentDescription = "Registros guardados en este dispositivo" }) {
                Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(R.drawable.ic_home_check), contentDescription = null, tint = Color(0xFF4D866E),
                        modifier = Modifier.size(12.dp))
                    Text("Local", style = estiloHome(12, FontWeight.Medium), color = Color(0xFF4D7865))
                }
            }
            Box {
                Surface(onClick = { mostrarPerfil = !mostrarPerfil }, shape = CircleShape,
                    color = Color(0xFFE0EFE8), border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.size(48.dp).testTag("home_perfil").semantics {
                        contentDescription = "Abrir menú de perfil"
                        stateDescription = if (mostrarPerfil) "Abierto" else "Cerrado"
                    }) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(iniciales, style = estiloHome(15, FontWeight.SemiBold), color = Color(0xFF3F7161))
                    }
                }
                DropdownMenu(
                    expanded = mostrarPerfil, onDismissRequest = { mostrarPerfil = false },
                    shape = RoundedCornerShape(22.dp), containerColor = Color.White,
                    tonalElevation = 0.dp, shadowElevation = 12.dp, offset = DpOffset(0.dp, 8.dp),
                    modifier = Modifier.widthIn(min = 210.dp).testTag("home_menu_perfil")
                ) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                        Text("Tu perfil", style = estiloHome(15, FontWeight.SemiBold), color = Brand.Ink)
                        Text("Tu espacio personal", style = estiloHome(12), color = Brand.Muted)
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = Brand.Border)
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión", style = estiloHome(14, FontWeight.Medium), color = Brand.Error) },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.ic_home_logout), contentDescription = null,
                                tint = Brand.Error, modifier = Modifier.size(20.dp))
                        },
                        onClick = { mostrarPerfil = false; onCerrarSesion() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActividadSemanalCard(dias: List<ActividadDiaUi>) {
    val total = dias.sumOf { it.cantidad }
    val maximo = dias.maxOfOrNull { it.cantidad }?.coerceAtLeast(1) ?: 1
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = Color.White,
        shadowElevation = 2.dp, border = BorderStroke(1.dp, Color(0xFFE8F1EB))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEAF5EE)) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_home_activity), contentDescription = null,
                            modifier = Modifier.size(22.dp), tint = Color(0xFF5F9276))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Tu actividad semanal", style = estiloHome(17, FontWeight.SemiBold), color = Brand.Ink)
                    Text("Últimos 7 días · $total registros", style = estiloHome(12), color = Brand.Muted)
                }
            }
            if (dias.isEmpty()) {
                Text("Todavía no hay actividad para mostrar.", style = estiloHome(14), color = Brand.Muted)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dias.forEach { dia ->
                        Column(Modifier.weight(1f).clearAndSetSemantics {
                            contentDescription = "${dia.etiqueta}: ${dia.cantidad} registros"
                        }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${dia.cantidad}", style = estiloHome(12, if (dia.esHoy) FontWeight.SemiBold else FontWeight.Normal),
                                color = if (dia.esHoy) Brand.Teal else Brand.Muted)
                            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.BottomCenter) {
                                Box(Modifier.widthIn(max = 24.dp).fillMaxWidth().height(56.dp)
                                    .background(Color(0xFFF3F7F4), RoundedCornerShape(8.dp)))
                                Box(Modifier.widthIn(max = 24.dp).fillMaxWidth().height((4f + 52f * dia.cantidad / maximo).dp)
                                    .background(Brush.verticalGradient(if (dia.esHoy) listOf(Color(0xFF34B9A5), Brand.Teal)
                                        else listOf(Color(0xFFC2E1CC), Color(0xFFA5CEB7))), RoundedCornerShape(8.dp)))
                            }
                            Text(dia.etiqueta, style = estiloHome(12, if (dia.esHoy) FontWeight.SemiBold else FontWeight.Normal),
                                color = if (dia.esHoy) Brand.Teal else Brand.Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaModulo(modulo: ModuloHome, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colores = paleta(modulo)
    Card(onClick = onClick, modifier = modifier.heightIn(min = 152.dp), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colores.fondo, contentColor = colores.acento)) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(colores.acento.copy(alpha = 0.035f), radius = 64.dp.toPx(),
                    center = Offset(size.width + 14.dp.toPx(), size.height + 6.dp.toPx()))
            }
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.65f)) {
                    Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                        Image(painterResource(colores.icono), contentDescription = null, modifier = Modifier.size(38.dp))
                    }
                }
                Text(modulo.titulo, style = estiloHome(16, FontWeight.SemiBold), color = colores.acento,
                    modifier = Modifier.heightIn(min = (44.8f * LocalDensity.current.fontScale).dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Registrar", style = estiloHome(12, FontWeight.Medium), color = colores.acento)
                    Icon(painterResource(R.drawable.ic_home_arrow), contentDescription = null,
                        tint = colores.acento, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun BarraInferiorHome(onAbrirSeccion: (SeccionHome) -> Unit) {
    Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), color = Color.White,
        shadowElevation = 8.dp) {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            SeccionHome.entries.forEach { seccion ->
                val icono = when (seccion) {
                    SeccionHome.INICIO -> R.drawable.ic_home_home
                    SeccionHome.HISTORIAL -> R.drawable.ic_home_history
                    SeccionHome.CONTENIDOS -> R.drawable.ic_home_content
                    SeccionHome.AJUSTES -> R.drawable.ic_home_settings
                }
                val titulo = when (seccion) {
                    SeccionHome.INICIO -> "Inicio"
                    SeccionHome.HISTORIAL -> "Historial"
                    SeccionHome.CONTENIDOS -> "Contenidos"
                    SeccionHome.AJUSTES -> "Ajustes"
                }
                NavigationBarItem(selected = seccion == SeccionHome.INICIO, onClick = { onAbrirSeccion(seccion) },
                    icon = { Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(21.dp)) },
                    label = { Text(titulo, style = estiloHome(12, if (seccion == SeccionHome.INICIO) FontWeight.SemiBold else FontWeight.Medium)) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF168677),
                        selectedTextColor = Color(0xFF168677), indicatorColor = Color(0xFFE0F2E9),
                        unselectedIconColor = Color(0xFF7A9189), unselectedTextColor = Color(0xFF657E75)))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    DailySyncTheme(darkTheme = false) {
        HomeScreen(HomeUiState(fecha = "domingo, 8 de noviembre", saludo = "Buenas tardes", iniciales = "MN",
            actividadSemanal = listOf(ActividadDiaUi("Lun", 1), ActividadDiaUi("Mar", 3), ActividadDiaUi("Mié", 2),
                ActividadDiaUi("Jue", 0), ActividadDiaUi("Vie", 4), ActividadDiaUi("Sáb", 2), ActividadDiaUi("Hoy", 1, true))),
            onAbrirModulo = {}, onAbrirSeccion = {})
    }
}
