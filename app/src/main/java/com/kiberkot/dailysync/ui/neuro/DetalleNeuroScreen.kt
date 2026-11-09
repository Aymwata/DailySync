package com.kiberkot.dailysync.ui.neuro

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.components.EstadoRegistro
import com.kiberkot.dailysync.ui.theme.BarrasClarasMarca
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.ui.theme.DailySyncBrand
import com.kiberkot.dailysync.viewmodel.NeuroUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleNeuroScreen(
    state: NeuroUiState,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    onReintentar: () -> Unit = {}
) {
    BarrasClarasMarca()
    val disponible = !state.isLoading && !state.cargaFallida && !state.noDisponible
    val estrategiaNombre = state.catalogoEstrategias.find {
        it.idEstrategia == state.idEstrategiaSeleccionada
    }?.nombreEstrategia ?: "No especificada"

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
                        "Detalle de Autorregulación",
                        fontFamily = DailySyncBrand.Font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditar, enabled = disponible) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = DailySyncBrand.Teal)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!disponible) {
            EstadoRegistro(state.isLoading, state.errorMsg, onReintentar, Modifier.padding(paddingValues))
            return@Scaffold
        }
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
                    .background(Color(0xFFE8F2FA)),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.ic_module_neuro), null, Modifier.size(48.dp))
            }

            TarjetaDetalleBrand(titulo = "Tipo de Actividad / Tarea", valor = state.tipoActividad.ifBlank { "Sin información" })
            TarjetaDetalleBrand(titulo = "Fecha y Hora del Registro", valor = state.fechaHora.ifBlank { "Fecha reciente" })
            TarjetaDetalleBrand(titulo = "Nivel de Sobrecarga Sensorial", valor = "${state.nivelSobrecarga} / 5")
            TarjetaDetalleBrand(titulo = "Estrategia Aplicada", valor = estrategiaNombre)
            TarjetaDetalleBrand(titulo = "Contexto / Entorno", valor = state.contexto.ifBlank { "Sin contexto registrado" })
            TarjetaDetalleBrand(titulo = "Efectividad / Resultado", valor = "${state.resultadoPercibido} / 5")

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
                Text("Editar Registro", fontFamily = DailySyncBrand.Font, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
                color = Color(0xFF466F97)
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
