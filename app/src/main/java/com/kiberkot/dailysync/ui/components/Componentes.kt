package com.kiberkot.dailysync.ui.components

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.semantics.LiveRegionMode

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.testTag

// 1. CHIP / TARJETA SELECCIONABLE EN CUADRÍCULA
@Composable
fun CategoriaChipGrid(
    categoria: CategoriaAnimo,
    emoji: String,
    seleccionada: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val borderColor by animateColorAsState(
        targetValue = if (seleccionada) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "borderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (seleccionada) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "containerColor"
    )

    Surface(
        onClick = onSelect,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier.height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 30.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            if (seleccionada) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Estructura visual compartida por las pantallas del módulo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DbtScaffold(titulo: String, onVolver: () -> Unit, ocupado: Boolean = false, contenido: LazyListScope.() -> Unit) {
    Scaffold(modifier = Modifier.imePadding(), topBar = {
        TopAppBar(title = {
            Column {
                Text("Habilidades DBT", style = MaterialTheme.typography.labelMedium)
                Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }, navigationIcon = {
            IconButton(onClick = onVolver, enabled = !ocupado) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("dbt_contenido"), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = contenido)
    }
}

@Composable
internal fun DbtSeccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
internal fun DbtError(error: String, onReintentar: () -> Unit, habilitado: Boolean = true) {
    Column {
        Text(error, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onReintentar, enabled = habilitado) { Text("Reintentar") }
    }
}

/** Confirmación visible solo cuando la escritura en la base ya terminó. */
@Composable
fun AvisoRegistroGuardado(visible: Boolean, editando: Boolean = false) {
    val teclado = LocalSoftwareKeyboardController.current
    LaunchedEffect(visible) {
        if (visible) teclado?.hide()
    }
    AnimatedVisibility(visible = visible) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE8F5E9),
            modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(Modifier.width(10.dp))
                Text(if (editando) "¡Cambios guardados!" else "¡Nuevo registro guardado!",
                    color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }
}

/** Estado bloqueante: nunca presentar valores anteriores como si fueran el registro solicitado. */
@Composable
internal fun EstadoRegistro(cargando: Boolean, error: String?, onReintentar: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (cargando) CircularProgressIndicator()
        else {
            Text(error ?: "No se encontró el registro solicitado.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onReintentar) { Text("Reintentar") }
        }
    }
}
