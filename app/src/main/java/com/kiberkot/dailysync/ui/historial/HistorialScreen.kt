package com.kiberkot.dailysync.ui.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.model.RegistroHistorial
import com.kiberkot.dailysync.viewmodel.HistorialUiState
import com.kiberkot.dailysync.utils.formatearFechaRegistro

@OptIn(ExperimentalMaterial3Api::class)
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
    val visibles = state.registrosVisibles

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DailySync",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Mi historial",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al inicio"
                        )
                    }
                },
                actions = {
                    Text(
                        text = "${visibles.size} registros",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor =
                        MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = "Tus registros están almacenados " +
                                "en este dispositivo.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.filtroSeleccionado == null,
                            onClick = { onSeleccionarFiltro(null) },
                            label = { Text("Todos") }
                        )
                    }

                    items(AmbitoRegistro.entries.toList()) { ambito ->
                        FilterChip(
                            selected =
                                state.filtroSeleccionado == ambito,
                            onClick = { onSeleccionarFiltro(ambito) },
                            label = { Text(ambito.titulo) }
                        )
                    }
                }
            }

            state.mensaje?.let { mensaje ->
                item {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            state.error?.let { error ->
                item {
                    Column {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(
                            onClick = onReintentar,
                            enabled = !state.eliminando
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            if (state.cargando) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth()
                    )
                }
            } else if (visibles.isEmpty() && state.error == null) {
                item {
                    Text(
                        text = if (state.filtroSeleccionado == null) {
                            "Todavía no tienes registros."
                        } else {
                            "No hay registros de este ámbito."
                        },
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(
                items = visibles,
                key = { "${it.ambito.name}:${it.id}" }
            ) { registro ->
                TarjetaHistorial(
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
            title = { Text("Eliminar registro") },
            text = {
                Text(
                    "Se eliminará «${registro.titulo}» del dispositivo. " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmarEliminar,
                    enabled = !state.eliminando
                ) {
                    Text(
                        text = if (state.eliminando) {
                            "Eliminando…"
                        } else {
                            "Eliminar"
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelarEliminar,
                    enabled = !state.eliminando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaHistorial(
    registro: RegistroHistorial,
    habilitada: Boolean,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = registro.ambito.titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = formatearFechaRegistro(registro.fechaHora),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = registro.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(registro.descripcion)

            Text(
                text = "Intensidad: ${registro.intensidad}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = if (registro.pendienteSync) {
                    "Guardado localmente · Pendiente de sincronización"
                } else {
                    "Sincronizado"
                },
                style = MaterialTheme.typography.bodySmall
            )

            TextButton(
                onClick = onVerDetalle,
                enabled = habilitada
            ) {
                Text("Ver detalle")
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onEditar,
                    enabled = habilitada &&
                            registro.ambito in setOf(
                        AmbitoRegistro.ANIMO,
                        AmbitoRegistro.DBT,
                        AmbitoRegistro.NEURODESARROLLO,
                        AmbitoRegistro.ADICCIONES
                    )
                ) {
                    Text("Editar")
                }
                TextButton(
                    onClick = onEliminar,
                    enabled = habilitada
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
