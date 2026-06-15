package com.worldcup26.reminder.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.worldcup26.reminder.data.local.MatchEntity
import java.util.TimeZone

/**
 * Writes a followed match into the device's primary calendar via the Calendar
 * Provider. Events created here are picked up by whatever sync adapter owns that
 * calendar (typically Google Calendar), so no separate calendar integration is
 * needed. Only matches the user explicitly follows are written.
 */
class CalendarWriter(private val context: Context) {

    /** Default match duration used to set the event end time. */
    private val matchDurationMillis = 2 * 60 * 60 * 1000L

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    /** Inserts the event and returns its id, or null if it could not be written. */
    fun upsertEvent(match: MatchEntity): Long? {
        if (!hasPermission()) return null
        val calendarId = primaryCalendarId() ?: return null

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, "⚽ ${match.team1} — ${match.team2}")
            put(
                CalendarContract.Events.DESCRIPTION,
                listOfNotNull(match.round, match.group).joinToString(" · "),
            )
            match.ground?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
            put(CalendarContract.Events.DTSTART, match.kickoffEpochMillis)
            put(CalendarContract.Events.DTEND, match.kickoffEpochMillis + matchDurationMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.let { ContentUris.parseId(it) }
    }

    fun deleteEvent(eventId: Long) {
        if (!hasPermission()) return
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        context.contentResolver.delete(uri, null, null)
    }

    /** Finds a writable calendar, preferring the account's primary one. */
    private fun primaryCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
        )
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            "${CalendarContract.Calendars.IS_PRIMARY} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val accessCol =
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
            while (cursor.moveToNext()) {
                val access = cursor.getInt(accessCol)
                if (access >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    return cursor.getLong(idCol)
                }
            }
        }
        return null
    }
}
