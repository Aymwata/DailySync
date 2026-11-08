package com.kiberkot.dailysync.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("daily_sync_session", Context.MODE_PRIVATE)

    fun guardarSesion(usuarioId: String) {
        prefs.edit()
            .putString("usuario_id", usuarioId)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    fun obtenerUsuarioId(): String {
        return prefs.getString("usuario_id", null) ?: "usr_demo_01"
    }

    fun estaLogueado(): Boolean {
        return prefs.getBoolean("is_logged_in", false) && !prefs.getString("usuario_id", null).isNullOrBlank()
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}