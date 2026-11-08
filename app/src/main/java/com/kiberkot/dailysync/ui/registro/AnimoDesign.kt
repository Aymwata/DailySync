package com.kiberkot.dailysync.ui.registro

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.theme.DailySyncBrand

internal data class EstiloAnimo(
    val titulo: String, val matiz: String, val fondo: Color, val acento: Color,
    @DrawableRes val icono: Int
)

private val estilosAnimo = mapOf(
    1 to EstiloAnimo("Tranquilo", "Calmo", Color(0xFFEAF5EE), Color(0xFF42765F), R.drawable.ic_mood_calma),
    2 to EstiloAnimo("Motivado", "Con energía", Color(0xFFFFF4DC), Color(0xFF94631D), R.drawable.ic_mood_energia),
    3 to EstiloAnimo("Ansioso", "Inquieto", Color(0xFFF2ECF8), Color(0xFF785B99), R.drawable.ic_mood_inquietud),
    4 to EstiloAnimo("Abrumado", "Estresado", Color(0xFFEBF3FA), Color(0xFF486F95), R.drawable.ic_mood_sobrecarga),
    5 to EstiloAnimo("Desanimado", "Triste", Color(0xFFFBEDE7), Color(0xFFA26753), R.drawable.ic_mood_desanimo)
)

internal fun estiloAnimo(id: Int, nombre: String) = estilosAnimo[id]
    ?: EstiloAnimo(nombre, "", Color(0xFFFFF4DC), Color(0xFF94631D), R.drawable.ic_module_animo)

internal fun textoAnimo(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = DailySyncBrand.Font, fontSize = size.sp, lineHeight = (size * 1.4f).sp,
    fontWeight = weight, letterSpacing = (-0.15).sp
)

internal val AnimoAccent = Color(0xFF42765F)
internal val AnimoSoft = Color(0xFFFFF4DC)
