package com.kiberkot.dailysync.ui.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.ui.theme.DailySyncTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    registro: RegistroAnimo?,
    onVolver: () -> Unit,
    onEliminar: (String) -> Unit,
    onEditar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoEliminar by rememberSaveable(registro?.id) {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DailySync",
                            style = MaterialTheme.typography.labelMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Detalle del registro",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al historial"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (registro == null) {
                item {
                    SeccionDetalle(titulo = "Registro no disponible") {
                        Text(
                            "No se encontró el registro solicitado. " +
                                    "Vuelve al historial para consultar " +
                                    "los registros disponibles."
                        )

                        TextButton(onClick = onVolver) {
                            Text("Volver al historial")
                        }
                    }
                }
            } else {
                item {
                    ResumenAnimo(registro = registro)
                }

                item {
                    SeccionDetalle(titulo = "Contexto o entorno") {
                        Text(
                            text = registro.contexto,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                item {
                    SeccionDetalle(titulo = "Nota personal") {
                        Text(
                            text = registro.notaOpcional
                                ?.takeIf { it.isNotBlank() }
                                ?: "No añadiste una nota a este registro.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                item {
                    SeccionDetalle(titulo = "Almacenamiento") {
                        Text(
                            text = if (registro.pendienteSync) {
                                "Guardado en este dispositivo. " +
                                        "Pendiente de sincronización."
                            } else {
                                "Registro sincronizado."
                            }
                        )
                    }
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEditar(registro.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Editar registro")
                        }

                        OutlinedButton(
                            onClick = {
                                mostrarDialogoEliminar = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor =
                                        MaterialTheme.colorScheme.error
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar registro")
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoEliminar && registro != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEliminar = false
            },
            title = {
                Text("Eliminar registro")
            },
            text = {
                Text(
                    "Se eliminará este registro del dispositivo. " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        onEliminar(registro.id)
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ResumenAnimo(
    registro: RegistroAnimo
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = "Estado de Ánimo",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = registro.categoriaNombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = registro.fechaHora,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text(
                text = "Intensidad: ${registro.nivelIntensidad}/5",
                style = MaterialTheme.typography.titleMedium
            )

            LinearProgressIndicator(
                progress = {
                    registro.nivelIntensidad
                        .coerceIn(1, 5) / 5f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

@Composable
private fun SeccionDetalle(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            contenido()
        }
    }
}