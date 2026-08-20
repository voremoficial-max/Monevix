package com.monevix.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Se dispara el último día de cada mes: muestra el aviso y agenda el siguiente. */
class MonthlyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannel(context)
        NotificationHelper.showMonthEndReminder(context)
        ReminderScheduler.scheduleNext(context)
    }
}
