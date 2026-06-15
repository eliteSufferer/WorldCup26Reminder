package com.worldcup26.reminder.data.remote

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Converts the openfootball date + "HH:mm UTC±N" strings into a timezone-anchored
 * [Instant]. The stadium offset varies per match (US/Canada/Mexico span UTC-4..-8),
 * so we must read the offset from each [MatchDto.time] rather than assume one zone.
 */
object KickoffParser {

    private val OFFSET_REGEX = Regex("""UTC([+-]\d{1,2})(?::?(\d{2}))?""", RegexOption.IGNORE_CASE)
    private val TIME_REGEX = Regex("""(\d{1,2}):(\d{2})""")

    /**
     * Returns the absolute kickoff instant, or null if [date]/[time] can't be parsed.
     */
    fun toInstant(date: String, time: String): Instant? {
        val localDate = runCatching { LocalDate.parse(date.trim()) }.getOrNull() ?: return null
        val localTime = parseTime(time) ?: return null
        val offset = parseOffset(time) ?: return null
        return OffsetDateTime.of(localDate, localTime, offset).toInstant()
    }

    private fun parseTime(time: String): LocalTime? {
        val m = TIME_REGEX.find(time) ?: return null
        val hour = m.groupValues[1].toIntOrNull() ?: return null
        val minute = m.groupValues[2].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }

    private fun parseOffset(time: String): ZoneOffset? {
        val m = OFFSET_REGEX.find(time) ?: return null
        val hours = m.groupValues[1].toIntOrNull() ?: return null
        val minutes = m.groupValues[2].toIntOrNull() ?: 0
        return runCatching {
            ZoneOffset.ofHoursMinutes(hours, if (hours < 0) -minutes else minutes)
        }.getOrNull()
    }
}
