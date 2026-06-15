package com.worldcup26.reminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A match the user is following. Kept in its own table so refreshing [MatchEntity]
 * never wipes the user's picks. [calendarEventId] / alarm flags let us clean up the
 * device calendar and pending alarm when the user un-follows a match.
 */
@Entity(tableName = "selections")
data class SelectionEntity(
    @PrimaryKey val matchId: String,
    val reminderMinutesBefore: Int = 30,
    val calendarEventId: Long? = null,
    val alarmScheduled: Boolean = false,
)
