package com.monevix.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.monevix.app.MainActivity

object NotificationHelper {
    const val CHANNEL_ID = "monevix_month_end_reminder"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorio de fin de mes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisa cuando termina el mes para revisar el resumen mensual de pagos."
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showMonthEndReminder(context: Context) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Fin de mes en Monevix")
            .setContentText("Ya terminó el mes. Revisa el resumen mensual y descarga el PDF cuando quieras.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Ya terminó el mes. Revisa el resumen mensual de pagos por trabajador y " +
                        "descarga el PDF cuando quieras desde la app."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            try {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // El usuario no concedió el permiso de notificaciones (Android 13+); se ignora.
            }
        }
    }
}
