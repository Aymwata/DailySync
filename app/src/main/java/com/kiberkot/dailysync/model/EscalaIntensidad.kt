package com.kiberkot.dailysync.model

fun etiquetaIntensidad(valor: Int): String = when (valor) {
    0 -> "Sin síntomas"
    1 -> "Leve"
    2 -> "Moderada"
    3 -> "Marcada"
    4 -> "Severa"
    else -> "Selecciona un nivel"
}

/** Los datos antiguos se muestran con su escala original hasta que el usuario los corrija. */
fun textoIntensidad(valor: Int, version: Int, maximoAnterior: Int = 5): String =
    if (valor < 0) "Selecciona un nivel"
    else if (version == 1) "$valor / $maximoAnterior · Escala anterior"
    else "$valor / 4 · ${etiquetaIntensidad(valor)}"

const val AVISO_ESCALA_ANTERIOR = "Este registro utiliza una escala anterior. Selecciona nuevamente la intensidad de 0 a 4 para guardar los cambios."
