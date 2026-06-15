package com.worldcup26.reminder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.worldcup26.reminder.R
import com.worldcup26.reminder.domain.KnockoutStage
import com.worldcup26.reminder.domain.Match
import com.worldcup26.reminder.ui.components.TeamLabel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val CARD_HEIGHT = 76.dp
private val FIRST_GAP = 14.dp
private val COLUMN_WIDTH = 184.dp

private val bracketTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, ", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
private val bracketClockFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())

/**
 * Left-to-right knockout bracket. Each round is a column; later rounds are spaced so
 * every match sits vertically centered between the two matches that feed it, which
 * reads as a bracket without drawing connector lines. Scrolls both axes since the
 * full bracket is far larger than a phone screen.
 */
@Composable
fun BracketView(matches: List<Match>, onToggleFollow: (Match) -> Unit) {
    if (matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.bracket_empty))
        }
        return
    }

    val byStage = matches.groupBy { it.stage }
    // Bracket-doubling stages, in order. Round of 32 = depth 0, Final = depth 4.
    val mainStages = listOf(
        KnockoutStage.ROUND_OF_32,
        KnockoutStage.ROUND_OF_16,
        KnockoutStage.QUARTER,
        KnockoutStage.SEMI,
        KnockoutStage.FINAL,
    )
    val unit = CARD_HEIGHT + FIRST_GAP

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        mainStages.forEachIndexed { depth, stage ->
            val stageMatches = byStage[stage].orEmpty().sortedBy { it.kickoff }
            if (stageMatches.isNotEmpty()) {
                BracketColumn(
                    label = stage.label,
                    matches = stageMatches,
                    depth = depth,
                    unit = unit,
                    onToggleFollow = onToggleFollow,
                )
            }
        }
        // Third-place match: shown as its own column without bracket centering.
        byStage[KnockoutStage.THIRD]?.takeIf { it.isNotEmpty() }?.let { third ->
            BracketColumn(
                label = KnockoutStage.THIRD.label,
                matches = third,
                depth = 0,
                unit = unit,
                onToggleFollow = onToggleFollow,
            )
        }
    }
}

@Composable
private fun BracketColumn(
    label: String,
    matches: List<Match>,
    depth: Int,
    unit: Dp,
    onToggleFollow: (Match) -> Unit,
) {
    val span = 1 shl depth // 2^depth
    val topPad = unit * (span - 1) / 2f
    val betweenGap = unit * span - CARD_HEIGHT

    Column(modifier = Modifier.width(COLUMN_WIDTH)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        )
        Spacer(Modifier.height(topPad))
        matches.forEachIndexed { index, match ->
            BracketCard(match = match, onToggleFollow = onToggleFollow)
            if (index != matches.lastIndex) Spacer(Modifier.height(betweenGap))
        }
    }
}

@Composable
private fun BracketCard(match: Match, onToggleFollow: (Match) -> Unit) {
    val team1Won = match.hasScore && match.scoreFt1!! > match.scoreFt2!!
    val team2Won = match.hasScore && match.scoreFt2!! > match.scoreFt1!!
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CARD_HEIGHT)
            .clickable { onToggleFollow(match) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = bracketTimeFormatter.format(match.kickoff) +
                        bracketClockFormatter.format(match.kickoff),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                )
                if (match.isFollowed) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = stringResource(R.string.action_unfollow),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(14.dp),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamLabel(match.team1, bold = team1Won, trailingScore = match.scoreFt1)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamLabel(match.team2, bold = team2Won, trailingScore = match.scoreFt2)
            }
        }
    }
}
