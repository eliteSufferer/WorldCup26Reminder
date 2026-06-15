package com.worldcup26.reminder.notify

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.worldcup26.reminder.R
import com.worldcup26.reminder.ui.MainActivity

/**
 * Fired by [com.worldcup26.reminder.work.AlarmScheduler] at (kickoff − reminder).
 * Posts a high-importance notification for the followed match.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: title

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return // User revoked notifications; nothing we can do here.
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            matchId.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, Notifications.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(matchId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_MATCH_ID = "extra_match_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
    }
}
