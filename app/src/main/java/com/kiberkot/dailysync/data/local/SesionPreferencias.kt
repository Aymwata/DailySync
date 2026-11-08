package com.kiberkot.dailysync.data.local

import android.content.Context

/** Solo guarda la cuenta local activa. No persiste contraseñas ni datos de formularios. */
class SesionPreferencias(context: Context, nombre: String = "sesion_local") {
    private val preferencias = context.getSharedPreferences(nombre, Context.MODE_PRIVATE)

    // Estas operaciones se ejecutan en IO; commit confirma el disco antes de cambiar de pantalla.
    fun obtenerUsuario(): String? = preferencias.getString("usuario_id", null)

    fun guardar(usuarioId: String) {
        check(preferencias.edit().putString("usuario_id", usuarioId).commit())
    }

    fun borrar() {
        check(preferencias.edit().clear().commit())
    }
}
