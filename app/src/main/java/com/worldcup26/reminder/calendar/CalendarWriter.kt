package com.worldcup26.reminder.calendar

import android.Manifest
import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.worldcup26.reminder.data.local.MatchEntity
import java.util.TimeZone

/** A writable calendar the user can pick as the reminder destination. */
data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    /** Local (on-device) calendars apply instantly; Google ones depend on sync. */
    val isLocal: Boolean,
)

/**
 * Writes a followed match into a device calendar via the Calendar Provider. Events
 * created here are picked up by whatever sync adapter owns that calendar (typically
 * Google Calendar), so no separate calendar integration is needed. Only matches the
 * user explicitly follows are written.
 */
class CalendarWriter(private val context: Context) {

    /** Default match duration used to set the event end time. */
    private val matchDurationMillis = 2 * 60 * 60 * 1000L

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Inserts the event and returns its id, or null if it could not be written.
     * Writes to [preferredCalendarId] when it is still a valid writable calendar,
     * otherwise falls back to the account's primary calendar.
     */
    fun upsertEvent(
        match: MatchEntity,
        preferredCalendarId: Long? = null,
        reminderMinutesBefore: Int? = null,
    ): Long? {
        if (!hasPermission()) return null
        val calendarId = resolveCalendarId(preferredCalendarId) ?: return null

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
            if (reminderMinutesBefore != null) put(CalendarContract.Events.HAS_ALARM, 1)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?: return null
        val eventId = ContentUris.parseId(uri)

        if (reminderMinutesBefore != null) {
            val reminder = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, reminderMinutesBefore)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminder)
        }

        requestSyncForCalendar(calendarId)
        return eventId
    }

    /**
     * Asks the calendar's account to sync now so a freshly-written event is pushed to
     * the server promptly instead of waiting for the next periodic sync. No-op for
     * local calendars (nothing to sync) and harmless if sync is unavailable.
     */
    private fun requestSyncForCalendar(calendarId: Long) {
        val (accountName, accountType) = accountForCalendar(calendarId) ?: return
        if (accountType == CalendarContract.ACCOUNT_TYPE_LOCAL) return
        runCatching {
            val extras = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
            ContentResolver.requestSync(
                Account(accountName, accountType),
                CalendarContract.AUTHORITY,
                extras,
            )
        }
    }

    private fun accountForCalendar(calendarId: Long): Pair<String, String>? {
        val uri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId)
        context.contentResolver.query(
            uri,
            arrayOf(
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0) ?: return null
                val type = cursor.getString(1) ?: return null
                return name to type
            }
        }
        return null
    }

    fun deleteEvent(eventId: Long) {
        if (!hasPermission()) return
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        context.contentResolver.delete(uri, null, null)
    }

    /** Lists writable calendars so the user can choose a reminder destination. */
    fun listCalendars(): List<CalendarInfo> {
        if (!hasPermission()) return emptyList()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val result = mutableListOf<CalendarInfo>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            // Only visible calendars — events in a hidden calendar would never show.
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.IS_PRIMARY} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameCol =
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountCol =
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            val accountTypeCol =
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)
            val accessCol =
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
            while (cursor.moveToNext()) {
                if (cursor.getInt(accessCol) >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    result += CalendarInfo(
                        id = cursor.getLong(idCol),
                        displayName = cursor.getString(nameCol) ?: "Calendar",
                        accountName = cursor.getString(accountCol) ?: "",
                        isLocal = cursor.getString(accountTypeCol) ==
                            CalendarContract.ACCOUNT_TYPE_LOCAL,
                    )
                }
            }
        }
        return result
    }

    /**
     * Honours the user's explicit pick when it is still valid; otherwise writes to the
     * app's own on-device calendar (created on first use), which applies instantly and
     * needs no account sync.
     */
    private fun resolveCalendarId(preferredCalendarId: Long?): Long? {
        if (preferredCalendarId != null && listCalendars().any { it.id == preferredCalendarId }) {
            return preferredCalendarId
        }
        return ensureLocalCalendarId()
    }

    /** Returns the id of the app's dedicated local calendar, creating it if needed. */
    private fun ensureLocalCalendarId(): Long? =
        findLocalCalendarId() ?: createLocalCalendar()

    private fun findLocalCalendarId(): Long? {
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID),
            "${CalendarContract.Calendars.ACCOUNT_TYPE} = ? AND " +
                "${CalendarContract.Calendars.NAME} = ?",
            arrayOf(CalendarContract.ACCOUNT_TYPE_LOCAL, LOCAL_CALENDAR_NAME),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return null
    }

    private fun createLocalCalendar(): Long? {
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, LOCAL_ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, LOCAL_CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, LOCAL_CALENDAR_DISPLAY_NAME)
            put(CalendarContract.Calendars.CALENDAR_COLOR, LOCAL_CALENDAR_COLOR)
            put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CAL_ACCESS_OWNER,
            )
            put(CalendarContract.Calendars.OWNER_ACCOUNT, LOCAL_ACCOUNT_NAME)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        }
        // Creating a calendar requires the sync-adapter URI with the account params.
        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, LOCAL_ACCOUNT_NAME)
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.ACCOUNT_TYPE_LOCAL,
            )
            .build()
        return context.contentResolver.insert(uri, values)?.let { ContentUris.parseId(it) }
    }

    companion object {
        private const val LOCAL_ACCOUNT_NAME = "WC26 Reminder"
        private const val LOCAL_CALENDAR_NAME = "wc26_reminders"
        private const val LOCAL_CALENDAR_DISPLAY_NAME = "WC26 Reminders"
        private const val LOCAL_CALENDAR_COLOR = 0xFF1B5E20.toInt()
    }
}
