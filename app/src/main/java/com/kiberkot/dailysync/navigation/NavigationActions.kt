package com.kiberkot.dailysync.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/** Entrada corta desde la derecha, sin fundido ni desplazamiento completo de pantalla. */
internal fun entradaRapida(): EnterTransition = slideInHorizontally(
    initialOffsetX = { ancho -> ancho / 10 },
    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
)

/** Descarta eventos de una pantalla saliente, sin bloquear el siguiente toque válido con un temporizador. */
internal fun NavHostController.navegarDesde(origen: NavBackStackEntry, ruta: String) {
    if (currentBackStackEntry != origen) return
    navigate(ruta) { launchSingleTop = true }
}

internal fun NavHostController.volverDesde(origen: NavBackStackEntry) {
    if (currentBackStackEntry == origen) popBackStack()
}
