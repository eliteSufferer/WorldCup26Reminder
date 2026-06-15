package com.worldcup26.reminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.worldcup26.reminder.R
import com.worldcup26.reminder.domain.Match
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val dayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    matches: List<Match>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onToggleFollow: (Match) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResourceTitle()) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                matches.isEmpty() && refreshing ->
                    CircularProgressIndicator()
                matches.isEmpty() ->
                    Text(textOf(R.string.empty_no_matches))
                else ->
                    MatchList(matches = matches, onToggleFollow = onToggleFollow)
            }
        }
    }
}

@Composable
private fun MatchList(matches: List<Match>, onToggleFollow: (Match) -> Unit) {
    val zone = ZoneId.systemDefault()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(matches, key = { it.id }) { match ->
            MatchCard(match = match, zone = zone, onToggleFollow = onToggleFollow)
        }
    }
}

@Composable
private fun MatchCard(match: Match, zone: ZoneId, onToggleFollow: (Match) -> Unit) {
    val local = match.kickoff.atZone(zone)
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = buildString {
                        append(dayFormatter.format(local))
                        append(" · ")
                        append(timeFormatter.format(local))
                        match.group?.let { append(" · $it") }
                        match.ground?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                if (match.hasScore) {
                    Text(
                        text = "${match.scoreFt1} : ${match.scoreFt2}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            IconButton(onClick = { onToggleFollow(match) }) {
                if (match.isFollowed) {
                    Icon(Icons.Filled.Star, contentDescription = "Unfollow")
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = "Follow")
                }
            }
        }
    }
}

@Composable
private fun stringResourceTitle(): String = textOf(R.string.title_matches)

@Composable
private fun textOf(resId: Int): String =
    androidx.compose.ui.res.stringResource(resId)
