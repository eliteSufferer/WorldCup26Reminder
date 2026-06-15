package com.worldcup26.reminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per fixture. [id] is stable across refreshes so that user selections and
 * scheduled alarms survive when the upstream JSON is updated (e.g. when a knockout
 * placeholder like "Winner Group A" is replaced by the real team).
 */
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val kickoffEpochMillis: Long,
    val team1: String,
    val team2: String,
    val group: String?,
    val round: String?,
    val ground: String?,
    val scoreFt1: Int?,
    val scoreFt2: Int?,
    val broadcaster: String? = null,
)
