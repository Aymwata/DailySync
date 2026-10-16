package com.kiberkot.dailysync.ui.registro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.ui.components.CampoTextoFormulario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarAnimoScreen(
    registro: RegistroAnimo?,
    categorias: List<CategoriaAnimo>,
    onGuardarCambios: (CategoriaAnimo, Int, String, String?) -> Boolean,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (registro == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Check-in") },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Registro no encontrado", modifier = Modifier.padding(16.dp))
            }
        }
        return
    }

    var categoriaSeleccionada by remember {
        mutableStateOf(categorias.firstOrNull { it.id == registro.categoriaId } ?: categorias.first())
    }
    var nivelIntensidad by remember { mutableFloatStateOf(registro.nivelIntensidad.toFloat()) }
    var contexto by remember { mutableStateOf(registro.contexto) }
    var notaOpcional by remember { mutableStateOf(registro.notaOpcional ?: "") }
    var errorContexto by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Check-in",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "1. Estado de ánimo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Selector rápido de categoría
            categorias.forEach { cat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = categoriaSeleccionada.id == cat.id,
                        onClick = { categoriaSeleccionada = cat }
                    )
                    Text(text = cat.nombre)
                }
            }

            // Slider de Intensidad
            Text(
                text = "2. Nivel de intensidad: ${nivelIntensidad.toInt()} / 5",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Slider(
                value = nivelIntensidad,
                onValueChange = { nivelIntensidad = it },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de Contexto
            CampoTextoFormulario(
                valor = contexto,
                etiqueta = "3. Contexto o entorno",
                error = errorContexto,
                onValorChange = {
                    contexto = it
                    if (it.trim().length in 3..60) errorContexto = null
                }
            )

            // Campo de Nota
            CampoTextoFormulario(
                valor = notaOpcional,
                etiqueta = "4. Nota personal opcional",
                error = null,
                onValorChange = { notaOpcional = it },
                lineasMinimas = 3
            )

            Button(
                onClick = {
                    val exito = onGuardarCambios(
                        categoriaSeleccionada,
                        nivelIntensidad.toInt(),
                        contexto,
                        notaOpcional.ifBlank { null }
                    )
                    if (!exito) {
                        errorContexto = "El contexto debe tener entre 3 y 60 caracteres."
                    }
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}