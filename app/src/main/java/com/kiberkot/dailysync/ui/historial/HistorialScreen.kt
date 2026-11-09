package com.kiberkot.dailysync.ui.historial

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.theme.BarrasClarasMarca
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.model.RegistroHistorial
import com.kiberkot.dailysync.ui.theme.DailySyncBrand
import com.kiberkot.dailysync.utils.formatearFechaRegistro
import com.kiberkot.dailysync.viewmodel.HistorialUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistorialScreen(
    state: HistorialUiState,
    onSeleccionarFiltro: (AmbitoRegistro?) -> Unit,
    onVerDetalle: (RegistroHistorial) -> Unit,
    onEditar: (RegistroHistorial) -> Unit,
    onSolicitarEliminar: (RegistroHistorial) -> Unit,
    onCancelarEliminar: () -> Unit,
    onConfirmarEliminar: () -> Unit,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    BarrasClarasMarca()
    val visibles = state.registrosVisibles

    Scaffold(
        modifier = modifier,
        containerColor = DailySyncBrand.Background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DailySyncBrand.Background,
                    titleContentColor = DailySyncBrand.Ink,
                    navigationIconContentColor = DailySyncBrand.Ink
                ),
                title = {
                    Column {
                        Text(
                            text = "DailySync",
                            fontFamily = DailySyncBrand.Font,
                            fontSize = 12.sp,
                            color = DailySyncBrand.Muted
                        )
                        Text(
                            text = "Mi historial",
                            fontSize = 22.sp,
                            fontFamily = DailySyncBrand.Font,
                            fontWeight = FontWeight.Bold,
                            color = DailySyncBrand.Ink
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al inicio"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DailySyncBrand.Teal.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = if (visibles.size == 1) "1 registro" else "${visibles.size} registros",
                            fontFamily = DailySyncBrand.Font,
                            color = DailySyncBrand.Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DailySyncBrand.Field,
                    border = BorderStroke(1.dp, DailySyncBrand.Border)
                ) {
                    Text(
                        text = "Tus registros se guardan en este dispositivo.",
                        fontFamily = DailySyncBrand.Font,
                        fontSize = 13.sp,
                        color = DailySyncBrand.Ink,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }

            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    FiltroHistorial("Todos", state.filtroSeleccionado == null,
                        DailySyncBrand.Teal, DailySyncBrand.Field) { onSeleccionarFiltro(null) }
                    AmbitoRegistro.entries.forEach { ambito ->
                        val estilo = estiloHistorial(ambito)
                        FiltroHistorial(ambito.titulo, state.filtroSeleccionado == ambito,
                            estilo.acento, estilo.fondo) { onSeleccionarFiltro(ambito) }
                    }
                }
            }

            state.mensaje?.let { mensaje ->
                item {
                    Text(
                        text = mensaje,
                        fontFamily = DailySyncBrand.Font,
                        color = DailySyncBrand.Teal,
                        fontSize = 13.sp,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }

            state.error?.let { error ->
                item {
                    Column {
                        Text(
                            text = error,
                            fontFamily = DailySyncBrand.Font,
                            color = DailySyncBrand.Error
                        )
                        TextButton(
                            onClick = onReintentar,
                            enabled = !state.eliminando
                        ) {
                            Text("Reintentar", fontFamily = DailySyncBrand.Font, color = DailySyncBrand.Teal)
                        }
                    }
                }
            }

            if (state.cargando) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DailySyncBrand.Teal)
                    }
                }
            } else if (visibles.isEmpty() && state.error == null) {
                item {
                    Text(
                        text = if (state.filtroSeleccionado == null) {
                            "Todavía no tienes registros."
                        } else {
                            "No hay registros de este ámbito."
                        },
                        fontFamily = DailySyncBrand.Font,
                        color = DailySyncBrand.Muted,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(
                items = visibles,
                key = { "${it.ambito.name}:${it.id}" }
            ) { registro ->
                TarjetaHistorialBrand(
                    registro = registro,
                    habilitada = !state.eliminando,
                    onVerDetalle = { onVerDetalle(registro) },
                    onEditar = { onEditar(registro) },
                    onEliminar = { onSolicitarEliminar(registro) }
                )
            }
        }
    }

    state.registroAEliminar?.let { registro ->
        AlertDialog(
            onDismissRequest = onCancelarEliminar,
            containerColor = DailySyncBrand.Field,
            title = {
                Text(
                    text = "Eliminar registro",
                    fontFamily = DailySyncBrand.Font,
                    fontWeight = FontWeight.Bold,
                    color = DailySyncBrand.Ink
                )
            },
            text = {
                Text(
                    text = "Se eliminará «${registro.titulo}» del dispositivo. Esta acción no se puede deshacer.",
                    fontFamily = DailySyncBrand.Font,
                    color = DailySyncBrand.Ink
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmarEliminar,
                    enabled = !state.eliminando
                ) {
                    Text(
                        text = if (state.eliminando) "Eliminando…" else "Eliminar",
                        fontFamily = DailySyncBrand.Font,
                        color = DailySyncBrand.Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelarEliminar,
                    enabled = !state.eliminando
                ) {
                    Text("Cancelar", fontFamily = DailySyncBrand.Font, color = DailySyncBrand.Muted)
                }
            }
        )
    }
}

private data class EstiloHistorial(val acento: Color, val fondo: Color, val icono: Int)

private fun estiloHistorial(ambito: AmbitoRegistro) = when (ambito) {
    AmbitoRegistro.ANIMO -> EstiloHistorial(Color(0xFF42765F), Color(0xFFEAF5EE), R.drawable.ic_module_animo)
    AmbitoRegistro.DBT -> EstiloHistorial(Color(0xFF795B9D), Color(0xFFF1EBF8), R.drawable.ic_module_dbt)
    AmbitoRegistro.NEURODESARROLLO -> EstiloHistorial(Color(0xFF466F97), Color(0xFFE8F2FA), R.drawable.ic_module_neuro)
    AmbitoRegistro.ADICCIONES -> EstiloHistorial(Color(0xFFA56150), Color(0xFFFBECE6), R.drawable.ic_module_adiccion)
}

@Composable
private fun FiltroHistorial(texto: String, selected: Boolean, acento: Color, fondo: Color, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, selectedContainerColor = fondo,
            labelColor = DailySyncBrand.Muted, selectedLabelColor = acento),
        border = BorderStroke(1.dp, if (selected) acento else DailySyncBrand.Border),
        label = { Text(texto, fontFamily = DailySyncBrand.Font, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaHistorialBrand(
    registro: RegistroHistorial,
    habilitada: Boolean,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val estilo = estiloHistorial(registro.ambito)
    Surface(onClick = onVerDetalle, enabled = habilitada,
        shape = RoundedCornerShape(24.dp), color = Color.White, contentColor = DailySyncBrand.Ink,
        border = BorderStroke(1.dp, DailySyncBrand.Border), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = estilo.fondo) {
                    Image(painterResource(estilo.icono), null, Modifier.size(42.dp).padding(4.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(registro.ambito.titulo, fontFamily = DailySyncBrand.Font, fontSize = 12.sp,
                        color = estilo.acento, fontWeight = FontWeight.SemiBold)
                    Text(formatearFechaRegistro(registro.fechaHora), fontFamily = DailySyncBrand.Font,
                        fontSize = 12.sp, color = DailySyncBrand.Muted)
                }
            }
            Text(registro.titulo, fontFamily = DailySyncBrand.Font, fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold, color = DailySyncBrand.Ink)
            Text(registro.descripcion, fontFamily = DailySyncBrand.Font, fontSize = 15.sp,
                lineHeight = 22.sp, color = DailySyncBrand.Muted)
            Text("Intensidad: ${registro.intensidad}", fontFamily = DailySyncBrand.Font,
                fontSize = 13.sp, color = estilo.acento)
            HorizontalDivider(color = DailySyncBrand.Border)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onVerDetalle, enabled = habilitada) {
                    Text("Ver detalle", fontFamily = DailySyncBrand.Font, fontSize = 14.sp,
                        color = DailySyncBrand.Teal, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onEditar, enabled = habilitada) {
                    Text("Editar", fontFamily = DailySyncBrand.Font, fontSize = 14.sp, color = DailySyncBrand.Teal)
                }
                TextButton(onClick = onEliminar, enabled = habilitada) {
                    Text("Eliminar", fontFamily = DailySyncBrand.Font, fontSize = 14.sp, color = DailySyncBrand.Error)
                }
            }
        }
    }
}
