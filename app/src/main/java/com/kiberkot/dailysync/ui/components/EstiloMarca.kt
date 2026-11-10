package com.kiberkot.dailysync.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

internal val FondoPantallaMarca = Color(0xFFF6FAF7)
internal val FondoSuaveMarca = Color(0xFFEAF5EF)

internal fun textoMarca(tamano: Int, peso: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Brand.Font, fontSize = tamano.sp, lineHeight = (tamano * 1.4f).sp,
    fontWeight = peso, letterSpacing = (-0.15).sp)

@Composable
internal fun CabeceraMarca(titulo: String, etiqueta: String, onVolver: () -> Unit,
    @DrawableRes icono: Int, ocupado: Boolean = false, fondoIcono: Color = FondoSuaveMarca,
    tinteIcono: Color? = Brand.Teal) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = Color.White) {
            IconButton(onClick = onVolver, enabled = !ocupado, modifier = Modifier.size(44.dp)) {
                Icon(painterResource(R.drawable.ic_form_back), "Volver", tint = Brand.Ink, modifier = Modifier.size(21.dp))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(etiqueta, style = textoMarca(12, FontWeight.Medium), color = Brand.Teal)
            Text(titulo, style = textoMarca(20, FontWeight.SemiBold), color = Brand.Ink)
        }
        Surface(shape = RoundedCornerShape(14.dp), color = fondoIcono) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                if (tinteIcono == null) Image(painterResource(icono), null, Modifier.size(34.dp))
                else Icon(painterResource(icono), null, Modifier.size(24.dp), tint = tinteIcono)
            }
        }
    }
}

@Composable
internal fun TarjetaMarca(modifier: Modifier = Modifier, fondo: Color = Color.White,
    contenido: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = fondo,
        contentColor = Brand.Ink, border = BorderStroke(1.dp, Brand.Border)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = contenido)
    }
}

@Composable
internal fun PieMarca(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), color = Color.White,
        border = BorderStroke(1.dp, Brand.Border)) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), content = contenido)
    }
}

@Composable
internal fun AccionMarca(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    habilitado: Boolean = true, guardando: Boolean = false, @DrawableRes icono: Int = R.drawable.ic_home_check) {
    val forma = RoundedCornerShape(18.dp)
    Button(onClick = onClick, enabled = habilitado && !guardando, shape = forma,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent,
            contentColor = Color.White, disabledContentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).alpha(if (habilitado || guardando) 1f else 0.45f)
            .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), forma)) {
        if (guardando) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        else Icon(painterResource(icono), null, Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(texto, style = textoMarca(16, FontWeight.SemiBold))
    }
}
