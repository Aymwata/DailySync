package com.kiberkot.dailysync.ui.adiccion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.ui.theme.DailySyncBrand
import com.kiberkot.dailysync.viewmodel.AdiccionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAdiccionScreen(
    state: AdiccionUiState,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accionNombre = state.catalogoAcciones.find {
        it.idAccionProtectora == state.idAccionSeleccionada
    }?.nombreAccion ?: "No especificada"

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
                    Text(
                        "Detalle de Craving",
                        fontFamily = DailySyncBrand.Font,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = DailySyncBrand.Teal)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(DailySyncBrand.Error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", fontSize = 28.sp)
            }

            TarjetaDetalleBrand(titulo = "Nivel de Impulso / Craving", valor = "${state.nivelImpulso} / 10")
            TarjetaDetalleBrand(titulo = "Desencadenante / Gatillo", valor = state.desencadenante.ifBlank { "Sin especificar" })
            TarjetaDetalleBrand(titulo = "Fecha y Hora del Registro", valor = state.fechaHora.ifBlank { "Fecha reciente" })
            TarjetaDetalleBrand(titulo = "Acción Protectora Aplicada", valor = accionNombre)
            TarjetaDetalleBrand(titulo = "Cumplimiento de Objetivo", valor = state.estadoObjetivo)

            Button(
                onClick = onEditar,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DailySyncBrand.Teal, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Registro", fontFamily = DailySyncBrand.Font, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TarjetaDetalleBrand(titulo: String, valor: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DailySyncBrand.Field,
        border = BorderStroke(1.dp, DailySyncBrand.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                fontFamily = DailySyncBrand.Font,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DailySyncBrand.Error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                fontFamily = DailySyncBrand.Font,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DailySyncBrand.Ink
            )
        }
    }
}