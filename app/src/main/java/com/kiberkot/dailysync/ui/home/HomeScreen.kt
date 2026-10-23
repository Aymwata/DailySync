package com.kiberkot.dailysync.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.ui.theme.DailySyncTheme
import com.kiberkot.dailysync.viewmodel.ModuloHome
import com.kiberkot.dailysync.viewmodel.SeccionHome
import com.kiberkot.dailysync.viewmodel.ActividadDiaUi
import com.kiberkot.dailysync.viewmodel.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAbrirModulo: (ModuloHome) -> Unit,
    onAbrirSeccion: (SeccionHome) -> Unit,
    onReintentar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mostrarSelector by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CabeceraHome(iniciales = state.iniciales)
        },
        bottomBar = {
            BarraInferiorHome(onAbrirSeccion = onAbrirSeccion)
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = state.fecha,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = state.saludo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onReintentar) { Text("Reintentar") }
                } else {
                    ActividadSemanalCard(dias = state.actividadSemanal)
                }
            }

            item {
                Button(
                    onClick = { mostrarSelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "+ Nuevo registro",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                Text(
                    text = "Ámbitos de autorreporte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(count = 2) { fila ->
                val modulos = ModuloHome.entries
                    .drop(fila * 2)
                    .take(2)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    modulos.forEach { modulo ->
                        TarjetaModulo(
                            modulo = modulo,
                            onClick = { onAbrirModulo(modulo) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Proyecto académico · Utiliza datos ficticios.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (mostrarSelector) {
        ModalBottomSheet(
            onDismissRequest = { mostrarSelector = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¿Qué quieres registrar?",
                    style = MaterialTheme.typography.titleLarge
                )

                ModuloHome.entries.forEach { modulo ->
                    TextButton(
                        onClick = {
                            mostrarSelector = false
                            onAbrirModulo(modulo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text("${modulo.simbolo}  ${modulo.titulo}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraHome(iniciales: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DailySync",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Lemac DataLab",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Guardado local",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActividadSemanalCard(
    dias: List<ActividadDiaUi>
) {
    val total = dias.sumOf { it.cantidad }
    val maximo = dias.maxOfOrNull { it.cantidad }
        ?.coerceAtLeast(1) ?: 1

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Actividad de los últimos 7 días",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$total registros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (dias.isEmpty()) {
                Text("Todavía no hay actividad para mostrar.")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dias.forEach { dia ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${dia.cantidad}",
                                style = MaterialTheme.typography.labelSmall
                            )

                            Box(
                                modifier = Modifier.height(64.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            (
                                                    4f + 60f *
                                                            dia.cantidad / maximo
                                                    ).dp
                                        ),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (dia.esHoy) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {}
                            }

                            Text(
                                text = dia.etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (dia.esHoy) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaModulo(
    modulo: ModuloHome,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo = when (modulo) {
        ModuloHome.ANIMO ->
            MaterialTheme.colorScheme.primaryContainer

        ModuloHome.DBT ->
            MaterialTheme.colorScheme.secondaryContainer

        ModuloHome.NEURODESARROLLO ->
            MaterialTheme.colorScheme.tertiaryContainer

        ModuloHome.ADICCIONES ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 132.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondo,
            contentColor = contentColorFor(fondo)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = modulo.simbolo,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = modulo.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Registrar →",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun BarraInferiorHome(
    onAbrirSeccion: (SeccionHome) -> Unit
) {
    NavigationBar {
        SeccionHome.entries.forEach { seccion ->
            val icono = when (seccion) {
                SeccionHome.INICIO -> Icons.Default.Home
                SeccionHome.HISTORIAL -> Icons.Default.List
                SeccionHome.CONTENIDOS -> Icons.Default.Info
                SeccionHome.AJUSTES -> Icons.Default.Settings
            }

            val titulo = when (seccion) {
                SeccionHome.INICIO -> "Inicio"
                SeccionHome.HISTORIAL -> "Historial"
                SeccionHome.CONTENIDOS -> "Contenidos"
                SeccionHome.AJUSTES -> "Ajustes"
            }

            NavigationBarItem(
                selected = seccion == SeccionHome.INICIO,
                onClick = { onAbrirSeccion(seccion) },
                icon = {
                    Icon(
                        imageVector = icono,
                        contentDescription = null
                    )
                },
                label = { Text(titulo) }
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun HomeScreenPreview() {
    DailySyncTheme(darkTheme = false) {
        HomeScreen(
            state = HomeUiState(
                fecha = "jueves, 22 de octubre",
                saludo = "Buenas noches 🌙",
                iniciales = "AV",
                actividadSemanal = listOf(
                    ActividadDiaUi("Vie", 0),
                    ActividadDiaUi("Sáb", 1),
                    ActividadDiaUi("Dom", 0),
                    ActividadDiaUi("Lun", 0),
                    ActividadDiaUi("Mar", 0),
                    ActividadDiaUi("Mié", 0),
                    ActividadDiaUi("Hoy", 1, esHoy = true)
                )
            ),
            onAbrirModulo = {},
            onAbrirSeccion = {}
        )
    }
}
