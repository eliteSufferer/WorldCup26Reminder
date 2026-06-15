package com.worldcup26.reminder.domain

import java.time.Instant

/**
 * UI-facing match model. [kickoff] is an absolute instant; the UI formats it into the
 * device's local timezone for display.
 */
data class Match(
    val id: String,
    val kickoff: Instant,
    val team1: String,
    val team2: String,
    val group: String?,
    val round: String?,
    val ground: String?,
    val scoreFt1: Int?,
    val scoreFt2: Int?,
    /** Null when the user is not following this match. */
    val reminderMinutesBefore: Int?,
) {
    val isFollowed: Boolean get() = reminderMinutesBefore != null
    val hasScore: Boolean get() = scoreFt1 != null && scoreFt2 != null
    val isUpcoming: Boolean get() = kickoff.isAfter(Instant.now())
    val title: String get() = "$team1 — $team2"

    /** Group-stage matches carry a group label; knockout matches do not. */
    val isKnockout: Boolean get() = group == null

    /** True once the match is over (kickoff + a typical match length has passed). */
    val isFinished: Boolean
        get() = kickoff.plusSeconds(FINISHED_AFTER_MINUTES * 60).isBefore(Instant.now())

    val stage: KnockoutStage? get() = KnockoutStage.fromRound(round)

    companion object {
        private const val FINISHED_AFTER_MINUTES = 150L
    }
}
