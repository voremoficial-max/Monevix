package com.monevix.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reagenda el recordatorio de fin de mes después de reiniciar el teléfono. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.createChannel(context)
            ReminderScheduler.scheduleNext(context)
        }
    }
}
