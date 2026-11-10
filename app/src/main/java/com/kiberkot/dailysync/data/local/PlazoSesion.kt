package com.kiberkot.dailysync.data.local

import android.content.Context
import android.os.SystemClock
import android.provider.Settings

const val INACTIVIDAD_SESION_MS = 5 * 60 * 1000L

data class InstanteActividad(val fechaMs: Long, val transcurridoMs: Long, val arranque: Int = -1)

fun interface RelojSesion {
    fun ahora(): InstanteActividad
}

class RelojSesionSistema(context: Context) : RelojSesion {
    private val arranque = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    }.getOrDefault(-1)

    override fun ahora() = InstanteActividad(System.currentTimeMillis(), SystemClock.elapsedRealtime(), arranque)
}

/** El reloj monotónico incluye reposo; cambios de hora o reinicios nunca amplían el plazo. */
fun tiempoRestanteSesion(ultima: InstanteActividad?, ahora: InstanteActividad,
    limiteMs: Long = INACTIVIDAD_SESION_MS): Long {
    if (ultima == null || ultima.fechaMs <= 0 || ultima.transcurridoMs < 0) return 0
    if (ultima.arranque >= 0 && ahora.arranque >= 0 && ultima.arranque != ahora.arranque) return 0
    val real = ahora.fechaMs - ultima.fechaMs
    val monotono = ahora.transcurridoMs - ultima.transcurridoMs
    if (real < 0 || monotono < 0) return 0
    return (limiteMs - maxOf(real, monotono)).coerceAtLeast(0)
}
