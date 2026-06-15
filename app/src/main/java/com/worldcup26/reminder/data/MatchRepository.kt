package com.worldcup26.reminder.data

import com.worldcup26.reminder.calendar.CalendarWriter
import com.worldcup26.reminder.data.local.MatchDao
import com.worldcup26.reminder.data.local.MatchEntity
import com.worldcup26.reminder.data.local.MatchWithSelection
import com.worldcup26.reminder.data.local.SelectionEntity
import com.worldcup26.reminder.data.remote.KickoffParser
import com.worldcup26.reminder.data.remote.MatchDto
import com.worldcup26.reminder.data.remote.ScheduleApi
import com.worldcup26.reminder.domain.Match
import com.worldcup26.reminder.work.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Single source of truth for schedule data and the user's follow list. Coordinates
 * the network fetch, local cache, exact alarms and calendar writes.
 */
class MatchRepository(
    private val api: ScheduleApi,
    private val dao: MatchDao,
    private val alarms: AlarmScheduler,
    private val calendar: CalendarWriter,
) {

    fun observeMatches(): Flow<List<Match>> =
        dao.observeMatches().map { rows -> rows.map { it.toDomain() } }

    /**
     * Pulls the latest schedule and upserts it. Selections are untouched, so existing
     * follows/alarms survive. Re-arms alarms for followed matches whose kickoff time
     * may have shifted upstream. Returns the number of matches stored.
     */
    suspend fun refresh(): Int {
        val schedule = api.fetchSchedule()
        val entities = schedule.matches.mapNotNull { it.toEntityOrNull() }
        dao.upsertMatches(entities)
        rescheduleFollowed()
        return entities.size
    }

    /** Calendars the user can choose between in settings. */
    fun availableCalendars() = calendar.listCalendars()

    suspend fun follow(matchId: String, reminderMinutesBefore: Int, calendarId: Long? = null) {
        val match = dao.getMatch(matchId) ?: return
        val eventId = calendar.upsertEvent(match, calendarId)
        dao.upsertSelection(
            SelectionEntity(
                matchId = matchId,
                reminderMinutesBefore = reminderMinutesBefore,
                calendarEventId = eventId,
                alarmScheduled = true,
            )
        )
        alarms.schedule(match, reminderMinutesBefore)
    }

    suspend fun unfollow(matchId: String) {
        val selection = dao.getSelection(matchId) ?: return
        alarms.cancel(matchId)
        selection.calendarEventId?.let { calendar.deleteEvent(it) }
        dao.deleteSelection(matchId)
    }

    /** Re-arms all pending alarms — used after refresh and after device reboot. */
    suspend fun rescheduleFollowed() {
        for (selection in dao.getAllSelections()) {
            val match = dao.getMatch(selection.matchId) ?: continue
            if (match.kickoffEpochMillis > System.currentTimeMillis()) {
                alarms.schedule(match, selection.reminderMinutesBefore)
            }
        }
    }

    private fun MatchDto.toEntityOrNull(): MatchEntity? {
        val instant = KickoffParser.toInstant(date, time) ?: return null
        return MatchEntity(
            id = stableId(instant),
            kickoffEpochMillis = instant.toEpochMilli(),
            team1 = team1,
            team2 = team2,
            group = group,
            round = round,
            ground = ground,
            scoreFt1 = score?.ft?.getOrNull(0),
            scoreFt2 = score?.ft?.getOrNull(1),
        )
    }

    /**
     * Deterministic id that is stable when the upstream JSON is re-published.
     * Knockout matches carry a stable [MatchDto.num]; group matches are keyed by
     * date + group (the fixture pairing within a group/day is fixed).
     */
    private fun MatchDto.stableId(instant: Instant): String = when {
        num != null -> "ko-$num"
        else -> "grp-${date}-${group ?: "x"}-${team1.slug()}-${team2.slug()}"
            .lowercase()
    }

    private fun String.slug(): String = trim().replace(Regex("[^A-Za-z0-9]+"), "")

    companion object {
        fun MatchWithSelection.toDomain(): Match = Match(
            id = id,
            kickoff = Instant.ofEpochMilli(kickoffEpochMillis),
            team1 = team1,
            team2 = team2,
            group = group,
            round = round,
            ground = ground,
            scoreFt1 = scoreFt1,
            scoreFt2 = scoreFt2,
            reminderMinutesBefore = reminderMinutesBefore,
        )

        private fun MatchEntity.toDomain(): Match = Match(
            id = id,
            kickoff = Instant.ofEpochMilli(kickoffEpochMillis),
            team1 = team1,
            team2 = team2,
            group = group,
            round = round,
            ground = ground,
            scoreFt1 = scoreFt1,
            scoreFt2 = scoreFt2,
            reminderMinutesBefore = null,
        )
    }
}
