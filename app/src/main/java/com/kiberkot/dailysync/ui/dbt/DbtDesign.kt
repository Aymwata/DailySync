package com.kiberkot.dailysync.ui.dbt

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.model.HabilidadDbt
import com.kiberkot.dailysync.ui.theme.BarrasClarasMarca
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

internal val DbtPurple = Color(0xFF795B9D)
internal val DbtLavender = Color(0xFFF1EBF8)

internal data class GuiaDbt(
    val modulo: String,
    val resumen: String,
    val pasos: List<String>,
    val fondo: Color,
    val acento: Color,
    @DrawableRes val icono: Int
)

// Texto de presentación breve: los identificadores y los registros del catálogo se conservan.
private val guiasDbt = mapOf(
    1 to GuiaDbt("Atención plena", "Decide teniendo en cuenta lo que sientes y los hechos.",
        listOf("Reconoce tu emoción y el impulso que aparece.", "Separa los hechos de tus suposiciones.",
            "Elige una respuesta que cuide tus necesidades y tus objetivos."),
        Color(0xFFEAF5EF), Color(0xFF467560), R.drawable.ic_dbt_wise),
    2 to GuiaDbt("Tolerancia al malestar", "Haz una pausa antes de actuar por impulso.",
        listOf("S · Detente antes de responder.", "T · Toma distancia y respira.",
            "O · Observa lo que pasa, piensas y sientes.", "P · Procede con atención a tu objetivo."),
        Color(0xFFFBEEE7), Color(0xFF9D614B), R.drawable.ic_dbt_stop),
    3 to GuiaDbt("Relaciones", "Pide lo que necesitas o di que no con claridad y respeto.",
        listOf("Describe los hechos y expresa cómo te afectan.",
            "Pide lo que necesitas o marca un límite. Explica el beneficio de un acuerdo.",
            "Mantén el foco y un tono seguro. Negocia alternativas."),
        Color(0xFFEBF3FB), Color(0xFF4B7098), R.drawable.ic_dbt_dear),
    4 to GuiaDbt("Regulación emocional", "Elige otra acción cuando seguir el impulso no te ayuda.",
        listOf("Identifica la emoción y lo que te impulsa a hacer.",
            "Revisa los hechos y comprueba que la situación es segura.",
            "Si el impulso no te ayuda, prueba una acción opuesta, pequeña y concreta."),
        DbtLavender, DbtPurple, R.drawable.ic_dbt_opposite)
)

internal fun guiaDbt(habilidad: HabilidadDbt): GuiaDbt = guiasDbt[habilidad.id]
    ?: GuiaDbt(habilidad.modulo, habilidad.contenido.substringBefore("\n\n"), emptyList(),
        DbtLavender, DbtPurple, R.drawable.ic_module_dbt)

internal fun textoDbt(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Brand.Font, fontSize = size.sp, lineHeight = (size * 1.4f).sp,
    fontWeight = weight, letterSpacing = (-0.15).sp
)

@Composable
internal fun DbtPagina(
    titulo: String, onVolver: () -> Unit, ocupado: Boolean = false,
    lista: LazyListState = rememberLazyListState(),
    pie: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    contenido: LazyListScope.() -> Unit
) {
    BarrasClarasMarca()
    Scaffold(modifier = modifier.fillMaxSize().imePadding(), containerColor = Color(0xFFF7F9F8),
        contentColor = Brand.Ink, topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = Color.White) {
                    IconButton(onClick = onVolver, enabled = !ocupado, modifier = Modifier.size(44.dp)) {
                        Icon(painterResource(R.drawable.ic_form_back), "Volver", Modifier.size(21.dp), tint = Brand.Ink)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("Habilidades DBT", style = textoDbt(12, FontWeight.Medium), color = DbtPurple)
                    Text(titulo, style = textoDbt(20, FontWeight.SemiBold))
                }
                Surface(shape = RoundedCornerShape(14.dp), color = DbtLavender) {
                    Image(painterResource(R.drawable.ic_module_dbt), null, Modifier.size(42.dp).padding(4.dp))
                }
            }
        }, bottomBar = pie) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("dbt_contenido"), state = lista,
            contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp), content = contenido)
    }
}

@Composable
internal fun DbtTarjeta(modifier: Modifier = Modifier, color: Color = Color.White,
    contenido: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = color, contentColor = Brand.Ink,
        border = BorderStroke(1.dp, Brand.Border)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido)
    }
}

@Composable
internal fun DbtPaso(numero: String, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = DbtLavender) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Text(numero, style = textoDbt(12, FontWeight.SemiBold), color = DbtPurple)
            }
        }
        Text(titulo, style = textoDbt(17, FontWeight.SemiBold))
    }
}

@Composable
internal fun DbtAccion(texto: String, onClick: () -> Unit, enabled: Boolean = true,
    guardando: Boolean = false, error: String? = null) {
    Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), color = Color.White,
        border = BorderStroke(1.dp, Brand.Border)) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            error?.let { DbtMensajeError(it) }
            val forma = RoundedCornerShape(18.dp)
            Button(onClick, enabled = enabled, shape = forma,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), forma)) {
                if (guardando) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp), tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text(texto, style = textoDbt(16, FontWeight.SemiBold), color = Color.White,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            Text("Se guarda en este dispositivo", style = textoDbt(12), color = Brand.Muted)
        }
    }
}

@Composable
internal fun DbtMensajeError(mensaje: String) {
    Text(mensaje, color = Brand.Error, style = textoDbt(12),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
internal fun DbtEstado(cargando: Boolean, error: String?, onReintentar: () -> Unit) {
    DbtTarjeta {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(24.dp), color = Brand.Teal, strokeWidth = 2.dp)
            Text("Cargando habilidades…", style = textoDbt(14), color = Brand.Muted)
        } else {
            DbtMensajeError(error ?: "No se encontraron los datos.")
            TextButton(onClick = onReintentar) { Text("Reintentar", style = textoDbt(14), color = Brand.Teal) }
        }
    }
}
