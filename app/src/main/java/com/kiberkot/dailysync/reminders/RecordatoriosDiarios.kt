package com.kiberkot.dailysync.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import com.kiberkot.dailysync.utils.NotificationHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface AgendaRecordatorios {
    fun programar(usuarioId: String, fechaMs: Long)
    fun cancelar(usuarioId: String)
}

interface AvisosRecordatorio {
    fun publicarRecordatorio(usuarioId: String): Boolean
    fun cancelarRecordatorio(usuarioId: String)
}

class AlarmasRecordatorio(private val context: Context) : AgendaRecordatorios {
    private val alarmas = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private fun pendiente(usuarioId: String): PendingIntent {
        val intent = Intent(context, RecordatorioReceiver::class.java)
            .setData(Uri.Builder().scheme("dailysync").authority("recordatorio").appendPath(usuarioId).build())
            .putExtra("usuario_id", usuarioId)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    override fun programar(usuarioId: String, fechaMs: Long) {
        // Recordatorio flexible: no necesita permisos de alarmas exactas ni un servicio permanente.
        alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fechaMs, pendiente(usuarioId))
    }
    override fun cancelar(usuarioId: String) { alarmas.cancel(pendiente(usuarioId)) }
}

fun horaRecordatorioValida(hora: String): Boolean = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]").matches(hora)

class RecordatoriosDiarios(
    context: Context,
    private val dao: DailySyncDao = AppDatabase.getDatabase(context).dailySyncDao(),
    private val agenda: AgendaRecordatorios = AlarmasRecordatorio(context),
    private val avisos: AvisosRecordatorio = NotificationHelper(context),
    private val ahora: () -> Calendar = { Calendar.getInstance() },
    nombrePreferencias: String = "recordatorios_enviados"
) {
    private val enviados = context.getSharedPreferences(nombrePreferencias, Context.MODE_PRIVATE)
    companion object { private val operaciones = Mutex() }

    private fun diasEnviados(usuarioId: String): Set<String> = when (val valor = enviados.all[usuarioId]) {
        is String -> setOf(valor)
        is Set<*> -> valor.filterIsInstance<String>().toSet()
        else -> emptySet()
    }

    private fun dia(calendario: Calendar): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = calendario.timeZone
    }.format(calendario.time)

    private fun horaDelDia(hora: String, hoy: Calendar): Calendar = (hoy.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hora.substring(0, 2).toInt())
        set(Calendar.MINUTE, hora.substring(3, 5).toInt())
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private fun programar(usuarioId: String, hora: String, siguienteDia: Boolean = false, actual: Calendar = ahora()) {
        val cita = horaDelDia(hora, actual)
        if (siguienteDia || dia(actual) in diasEnviados(usuarioId)) {
            cita.add(Calendar.DAY_OF_YEAR, 1)
        }
        // Si se activa después de la hora, comprueba hoy; nunca genera avisos retroactivos de otros días.
        agenda.programar(usuarioId, maxOf(cita.timeInMillis, actual.timeInMillis + 1000))
    }

    suspend fun configurar(usuarioId: String, activo: Boolean, hora: String) = operaciones.withLock {
        require(horaRecordatorioValida(hora))
        check(dao.configurarRecordatorio(usuarioId, activo, hora) == 1) { "La cuenta no está disponible." }
        agenda.cancelar(usuarioId)
        if (activo) programar(usuarioId, hora) else avisos.cancelarRecordatorio(usuarioId)
    }

    suspend fun reprogramarTodos() = operaciones.withLock {
        dao.usuariosConRecordatorio().forEach { usuario ->
            val hora = usuario.horaRecordatorio ?: "20:00"
            if (horaRecordatorioValida(hora)) programar(usuario.idUsuario, hora)
        }
    }

    suspend fun comprobar(usuarioId: String) = operaciones.withLock {
        val usuario = dao.obtenerUsuario(usuarioId)
        if (usuario == null || !usuario.recordatorioActivo) {
            agenda.cancelar(usuarioId)
            avisos.cancelarRecordatorio(usuarioId)
            return@withLock
        }
        val hora = usuario.horaRecordatorio ?: "20:00"
        if (!horaRecordatorioValida(hora)) { agenda.cancelar(usuarioId); return@withLock }
        val actual = ahora()
        if (actual.timeInMillis < horaDelDia(hora, actual).timeInMillis) {
            programar(usuarioId, hora)
            return@withLock
        }
        val hoy = dia(actual)
        val manana = (actual.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        if (dao.tieneRegistroEnPeriodo(usuarioId, hoy, dia(manana))) {
            avisos.cancelarRecordatorio(usuarioId)
        } else if (hoy !in diasEnviados(usuarioId) && avisos.publicarRecordatorio(usuarioId)) {
            check(enviados.edit().putStringSet(usuarioId, diasEnviados(usuarioId) + hoy).commit())
            // Cubre un guardado que haya terminado justo entre la consulta y la publicación.
            if (dao.tieneRegistroEnPeriodo(usuarioId, hoy, dia(manana))) avisos.cancelarRecordatorio(usuarioId)
        }
        programar(usuarioId, hora, siguienteDia = true, actual = actual)
    }

    fun reintentar(usuarioId: String) {
        agenda.programar(usuarioId, ahora().timeInMillis + 15 * 60 * 1000L)
    }
}
