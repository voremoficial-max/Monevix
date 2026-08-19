package com.monevix.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Programa un recordatorio para el último día de cada mes a las 6:00 p.m.
 *
 * Usa `setAndAllowWhileIdle`, que no requiere el permiso especial de
 * "alarmas exactas" (SCHEDULE_EXACT_ALARM): el recordatorio puede sonar con
 * algunos minutos de diferencia si el teléfono está en reposo profundo, lo
 * cual es aceptable para un aviso informativo de fin de mes.
 *
 * Este recordatorio SOLO avisa que terminó el mes; no descarga ningún PDF
 * automáticamente. El PDF se descarga cuando el usuario lo pide, desde el
 * resumen mensual o desde el detalle de cualquier liquidación.
 */
object ReminderScheduler {
    private const val REQUEST_CODE = 2001

    fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = nextMonthEndMillis()

        val intent = Intent(context, MonthlyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (_: SecurityException) {
            // Si el sistema restringe la programación de alarmas, simplemente no se agenda.
        }
    }

    private fun nextMonthEndMillis(): Long {
        val now = Calendar.getInstance()
        val candidate = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (candidate.timeInMillis <= now.timeInMillis) {
            candidate.add(Calendar.MONTH, 1)
            candidate.set(Calendar.DAY_OF_MONTH, candidate.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return candidate.timeInMillis
    }
}
