package com.kiberkot.dailysync.utils

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Admite los registros de Ánimo y DBT, aunque DBT conserve segundos en la base.
fun leerFechaRegistro(valor: String): Date? {
    if (valor.length < 16) return null
    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply {
        isLenient = false
    }
    val posicion = ParsePosition(0)
    val fecha = formato.parse(valor.take(16), posicion)
    return fecha?.takeIf { posicion.index == 16 }
}

fun formatearFechaRegistro(valor: String): String {
    val fecha = leerFechaRegistro(valor) ?: return valor
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(fecha)
}
