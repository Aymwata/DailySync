package com.kiberkot.dailysync.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kiberkot.dailysync.MainActivity
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.reminders.AvisosRecordatorio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationHelper(private val context: Context) : AvisosRecordatorio {
    companion object {
        const val CHANNEL_ID = "dailysync_recordatorio_diario"
        private const val AVISO_ID = 1
    }
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun eliminarCanalAnterior() {
        if (Build.VERSION.SDK_INT >= 26) manager.deleteNotificationChannel("dailysync_checkin_channel")
        else {
            val control = context.getSharedPreferences("notificaciones_locales", Context.MODE_PRIVATE)
            if (!control.getBoolean("confirmaciones_retiradas", false)) {
                manager.cancelAll()
                control.edit().putBoolean("confirmaciones_retiradas", true).apply()
            }
        }
    }

    fun prepararCanal() {
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Recordatorio diario",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Aviso discreto para el registro diario"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            })
        }
    }

    fun permitidas(): Boolean {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) return false
        return true
    }

    override fun publicarRecordatorio(usuarioId: String): Boolean {
        prepararCanal()
        if (!permitidas()) return false
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        val abrir = Intent(context, MainActivity::class.java)
            .setData(Uri.Builder().scheme("dailysync").authority("aviso").appendPath(usuarioId).build())
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendiente = PendingIntent.getActivity(context, 0, abrir,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val publica = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome).setContentTitle("DailySync")
            .setContentText("Aún estas a tiempo para tu registro diario! Ingresa para continuar.").build()
        val aviso = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome).setContentTitle("DailySync")
            .setContentText("Tienes un nuevo recordatorio.")
            .setCategory(NotificationCompat.CATEGORY_REMINDER).setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(publica)
            .setContentIntent(pendiente).setOnlyAlertOnce(true).setAutoCancel(true).build()
        manager.notify(usuarioId, AVISO_ID, aviso)
        return true
    }

    override fun cancelarRecordatorio(usuarioId: String) { manager.cancel(usuarioId, AVISO_ID) }

    /** Guardar nunca publica avisos; sólo retira un recordatorio que ya fue atendido hoy. */
    fun cancelarSiRegistroDeHoy(usuarioId: String, fechaOriginal: String? = null) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
        if (fechaOriginal == null || fechaOriginal.startsWith(hoy)) cancelarRecordatorio(usuarioId)
    }
}
