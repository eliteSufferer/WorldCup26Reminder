package com.worldcup26.reminder.domain

/**
 * Knockout rounds in bracket order, mapped from the openfootball `round` labels to
 * the short fraction labels used in the UI (1/16 = Round of 32, etc.).
 */
enum class KnockoutStage(val roundName: String, val label: String) {
    ROUND_OF_32("Round of 32", "1/16"),
    ROUND_OF_16("Round of 16", "1/8"),
    QUARTER("Quarter-final", "1/4"),
    SEMI("Semi-final", "1/2"),
    FINAL("Final", "Final"),
    THIRD("Match for third place", "3rd");

    companion object {
        fun fromRound(round: String?): KnockoutStage? =
            entries.firstOrNull { it.roundName == round }
    }
}
