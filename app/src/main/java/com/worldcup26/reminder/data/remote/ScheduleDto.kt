package com.worldcup26.reminder.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the openfootball worldcup.json schema:
 * https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json
 *
 * Most fields are optional because future (unplayed) matches omit scores/goals,
 * and knockout matches omit [group] while adding [num].
 */
@Serializable
data class ScheduleDto(
    val name: String,
    val matches: List<MatchDto> = emptyList(),
)

@Serializable
data class MatchDto(
    val round: String? = null,
    val num: Int? = null,
    val date: String,
    /** e.g. "13:00 UTC-6" — the UTC offset is per-match and must be parsed out. */
    val time: String,
    val team1: String,
    val team2: String,
    val group: String? = null,
    val ground: String? = null,
    val score: ScoreDto? = null,
)

@Serializable
data class ScoreDto(
    val ft: List<Int>? = null,
    val ht: List<Int>? = null,
)
