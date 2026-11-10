package com.kiberkot.dailysync.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*

class RecordatorioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val usuarioId = intent.getStringExtra("usuario_id") ?: return
        val pendiente = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val recordatorios = RecordatoriosDiarios(context.applicationContext)
            try {
                withTimeout(8000) { recordatorios.comprobar(usuarioId) }
            } catch (e: Exception) {
                Log.w("DailySync", "No se pudo comprobar el recordatorio", e)
                runCatching { recordatorios.reintentar(usuarioId) }
            } finally { pendiente.finish() }
        }
    }
}

class ReprogramarRecordatoriosReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val pendiente = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeout(8000) { RecordatoriosDiarios(context.applicationContext).reprogramarTodos() }
            } catch (e: Exception) {
                Log.w("DailySync", "No se pudieron reprogramar los recordatorios", e)
            } finally { pendiente.finish() }
        }
    }
}
