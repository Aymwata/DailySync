package com.kiberkot.dailysync.data.local

import android.content.Context

data class AceptacionConsentimiento(val version: Int, val fechaUtc: Long)

/** Metadatos locales por cuenta; cerrar sesión no elimina la aceptación. */
class ConsentimientoPreferencias(context: Context, nombre: String = "consentimiento_local") {
    private val preferencias = context.getSharedPreferences(nombre, Context.MODE_PRIVATE)

    fun obtener(usuarioId: String): AceptacionConsentimiento? {
        val version = preferencias.getInt("$usuarioId.version", 0)
        val fecha = preferencias.getLong("$usuarioId.fecha", 0)
        return if (version > 0 && fecha > 0) AceptacionConsentimiento(version, fecha) else null
    }

    fun aceptar(usuarioId: String, version: Int, fechaUtc: Long): AceptacionConsentimiento {
        require(usuarioId.isNotBlank() && version > 0 && fechaUtc > 0)
        check(preferencias.edit().putInt("$usuarioId.version", version)
            .putLong("$usuarioId.fecha", fechaUtc).commit()) {
            "No se pudo guardar el consentimiento."
        }
        return AceptacionConsentimiento(version, fechaUtc)
    }
}
