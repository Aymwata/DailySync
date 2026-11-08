package com.kiberkot.dailysync.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.kiberkot.dailysync.R

/** Base visual para adoptar progresivamente en las siguientes pantallas. */
internal object DailySyncBrand {
    val Ink = Color(0xFF163F3B)
    val Muted = Color(0xFF567B75)
    val Teal = Color(0xFF009C91)
    val Emerald = Color(0xFF009F72)
    val Background = Color(0xFFF0FBF8)
    val Field = Color(0xFFF5FAF8)
    val Border = Color(0xFFDDEEE8)
    val Error = Color(0xFFB43D46)
    val Font = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold)
    )
}
