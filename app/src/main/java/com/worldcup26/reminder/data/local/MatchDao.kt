package com.worldcup26.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * A match joined with its (optional) selection row. [reminderMinutesBefore] is null
 * when the user is not following the match.
 */
data class MatchWithSelection(
    val id: String,
    val kickoffEpochMillis: Long,
    val team1: String,
    val team2: String,
    val group: String?,
    val round: String?,
    val ground: String?,
    val scoreFt1: Int?,
    val scoreFt2: Int?,
    val broadcaster: String?,
    val reminderMinutesBefore: Int?,
    val calendarEventId: Long?,
)

@Dao
interface MatchDao {

    @Upsert
    suspend fun upsertMatches(matches: List<MatchEntity>)

    @Query(
        """
        SELECT m.*, s.reminderMinutesBefore AS reminderMinutesBefore,
               s.calendarEventId AS calendarEventId
        FROM matches m
        LEFT JOIN selections s ON s.matchId = m.id
        ORDER BY m.kickoffEpochMillis ASC
        """
    )
    fun observeMatches(): Flow<List<MatchWithSelection>>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatch(id: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSelection(selection: SelectionEntity)

    @Query("SELECT * FROM selections WHERE matchId = :matchId")
    suspend fun getSelection(matchId: String): SelectionEntity?

    @Query("SELECT * FROM selections")
    suspend fun getAllSelections(): List<SelectionEntity>

    @Query("DELETE FROM selections WHERE matchId = :matchId")
    suspend fun deleteSelection(matchId: String)
}
