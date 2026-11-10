package com.kiberkot.dailysync.data.local

import android.content.Context

data class SesionLocal(val usuarioId: String, val actividad: InstanteActividad?)

/** Guarda la cuenta activa y su última actividad; no almacena contraseñas ni formularios. */
class SesionPreferencias(context: Context, nombre: String = "sesion_local") {
    private val preferencias = context.getSharedPreferences(nombre, Context.MODE_PRIVATE)
    private val reloj = RelojSesionSistema(context)

    // Estas operaciones se ejecutan en IO; commit confirma el disco antes de cambiar de pantalla.
    fun obtenerUsuario(): String? = preferencias.getString("usuario_id", null)

    fun obtenerSesion(): SesionLocal? {
        val usuario = obtenerUsuario() ?: return null
        val actividad = if (preferencias.contains("actividad_fecha") && preferencias.contains("actividad_transcurrido")) {
            InstanteActividad(preferencias.getLong("actividad_fecha", 0),
                preferencias.getLong("actividad_transcurrido", -1), preferencias.getInt("actividad_arranque", -1))
        } else null
        return SesionLocal(usuario, actividad)
    }

    fun guardar(usuarioId: String, actividad: InstanteActividad = reloj.ahora()) {
        check(preferencias.edit().putString("usuario_id", usuarioId)
            .putLong("actividad_fecha", actividad.fechaMs).putLong("actividad_transcurrido", actividad.transcurridoMs)
            .putInt("actividad_arranque", actividad.arranque).commit())
    }

    // apply actualiza la memoria inmediatamente y serializa las escrituras de disco sin bloquear la UI.
    fun actualizarActividad(usuarioId: String, actividad: InstanteActividad) {
        if (obtenerUsuario() != usuarioId) return
        preferencias.edit().putLong("actividad_fecha", actividad.fechaMs)
            .putLong("actividad_transcurrido", actividad.transcurridoMs)
            .putInt("actividad_arranque", actividad.arranque).apply()
    }

    fun borrar() {
        check(preferencias.edit().clear().commit())
    }

    fun revocar() {
        preferencias.edit().remove("usuario_id").apply()
    }
}
