package com.worldcup26.reminder.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import com.worldcup26.reminder.data.local.MatchEntity
import com.worldcup26.reminder.notify.ReminderReceiver

/**
 * Schedules one exact alarm per followed match at (kickoff − reminderMinutes).
 * Exact alarms (vs WorkManager) are used because a reminder that is even a few
 * minutes late is useless — the match has already started.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService<AlarmManager>()!!

    fun schedule(match: MatchEntity, reminderMinutesBefore: Int) {
        val triggerAt = match.kickoffEpochMillis - reminderMinutesBefore * 60_000L
        if (triggerAt <= System.currentTimeMillis()) return // Too late to be useful.
        if (!canScheduleExact()) return // Caller should have requested the permission.

        val subtitle = buildString {
            append("Kicks off in $reminderMinutesBefore min")
            match.ground?.let { append(" · $it") }
        }
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_MATCH_ID, match.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, "${match.team1} — ${match.team2}")
            putExtra(ReminderReceiver.EXTRA_SUBTITLE, subtitle)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(match.id, intent),
        )
    }

    fun cancel(matchId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        alarmManager.cancel(pendingIntent(matchId, intent))
    }

    private fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun pendingIntent(matchId: String, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            matchId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
