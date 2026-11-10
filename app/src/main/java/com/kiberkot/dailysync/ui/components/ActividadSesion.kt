package com.kiberkot.dailysync.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalActividadSesion = staticCompositionLocalOf<() -> Boolean> { { true } }

/** El teclado en pantalla puede editar texto sin enviar eventos de tecla a la Activity. */
@Composable
fun <T> conActividadSesion(onCambio: (T) -> Unit): (T) -> Unit {
    val registrar = LocalActividadSesion.current
    return { valor -> if (registrar()) onCambio(valor) }
}
