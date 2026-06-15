package com.worldcup26.reminder.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.worldcup26.reminder.R

/** Notification channel + id constants shared by the reminder receiver. */
object Notifications {

    const val CHANNEL_REMINDERS = "match_reminders"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.channel_reminders_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_reminders_desc)
        }
        manager.createNotificationChannel(channel)
    }
}
