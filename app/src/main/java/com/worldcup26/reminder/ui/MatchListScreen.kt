package com.worldcup26.reminder.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.worldcup26.reminder.R
import com.worldcup26.reminder.data.settings.SettingsRepository
import com.worldcup26.reminder.domain.Match
import com.worldcup26.reminder.ui.components.SwipeSnackbarHost
import com.worldcup26.reminder.ui.components.TeamLabel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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
    groupMatches: List<Match>,
    knockoutMatches: List<Match>,
    groups: List<String>,
    filter: MatchFilter,
    refreshing: Boolean,
    initialTab: Int,
    events: Flow<UiEvent>,
    onRefresh: () -> Unit,
    onToggleFollow: (Match) -> Unit,
    onQueryChange: (String) -> Unit,
    onGroupChange: (String?) -> Unit,
    onToggleOnlyFollowed: () -> Unit,
    onToggleShowPrevious: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var tab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        events.collect { event ->
            val message = when (event) {
                is UiEvent.Saved -> context.getString(
                    if (event.addedToCalendar) R.string.snackbar_saved_calendar
                    else R.string.snackbar_saved_no_calendar
                )
                UiEvent.Removed -> context.getString(R.string.snackbar_removed)
                is UiEvent.Refreshed ->
                    context.getString(R.string.snackbar_refreshed, event.count)
                UiEvent.RefreshFailed -> context.getString(R.string.snackbar_refresh_failed)
            }
            snackbarHostState.currentSnackbarData?.dismiss()
            // Show ~2s; swipe (SwipeSnackbarHost) dismisses earlier.
            coroutineScope {
                val shown = launch {
                    snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Indefinite)
                }
                delay(2_000)
                shown.cancel()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_matches)) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
        snackbarHost = { SwipeSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == SettingsRepository.TAB_GROUPS,
                    onClick = { tab = SettingsRepository.TAB_GROUPS },
                    text = { Text(stringResource(R.string.tab_groups)) },
                )
                Tab(
                    selected = tab == SettingsRepository.TAB_PLAYOFF,
                    onClick = { tab = SettingsRepository.TAB_PLAYOFF },
                    text = { Text(stringResource(R.string.tab_playoff)) },
                )
            }
            when (tab) {
                SettingsRepository.TAB_PLAYOFF -> BracketView(
                    matches = knockoutMatches,
                    onToggleFollow = onToggleFollow,
                )
                else -> GroupsTab(
                    matches = groupMatches,
                    groups = groups,
                    filter = filter,
                    refreshing = refreshing,
                    onToggleFollow = onToggleFollow,
                    onQueryChange = onQueryChange,
                    onGroupChange = onGroupChange,
                    onToggleOnlyFollowed = onToggleOnlyFollowed,
                    onToggleShowPrevious = onToggleShowPrevious,
                )
            }
        }
    }
}

@Composable
private fun GroupsTab(
    matches: List<Match>,
    groups: List<String>,
    filter: MatchFilter,
    refreshing: Boolean,
    onToggleFollow: (Match) -> Unit,
    onQueryChange: (String) -> Unit,
    onGroupChange: (String?) -> Unit,
    onToggleOnlyFollowed: () -> Unit,
    onToggleShowPrevious: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        FilterBar(
            groups = groups,
            filter = filter,
            onQueryChange = onQueryChange,
            onGroupChange = onGroupChange,
            onToggleOnlyFollowed = onToggleOnlyFollowed,
            onToggleShowPrevious = onToggleShowPrevious,
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                matches.isEmpty() && refreshing -> CircularProgressIndicator()
                matches.isEmpty() -> Text(stringResource(R.string.empty_no_matches))
                else -> MatchList(matches = matches, onToggleFollow = onToggleFollow)
            }
        }
    }
}

@Composable
private fun FilterBar(
    groups: List<String>,
    filter: MatchFilter,
    onQueryChange: (String) -> Unit,
    onGroupChange: (String?) -> Unit,
    onToggleOnlyFollowed: () -> Unit,
    onToggleShowPrevious: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = filter.query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.filter_search_team)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GroupDropdown(groups = groups, selected = filter.group, onSelect = onGroupChange)
            FilterChip(
                selected = filter.onlyFollowed,
                onClick = onToggleOnlyFollowed,
                label = { Text(stringResource(R.string.filter_followed)) },
            )
            FilterChip(
                selected = filter.showPrevious,
                onClick = onToggleShowPrevious,
                label = { Text(stringResource(R.string.filter_show_previous)) },
            )
        }
    }
}

@Composable
private fun GroupDropdown(
    groups: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allLabel = stringResource(R.string.filter_all_groups)
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected ?: allLabel)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = { onSelect(null); expanded = false },
            )
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group) },
                    onClick = { onSelect(group); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun MatchList(matches: List<Match>, onToggleFollow: (Match) -> Unit) {
    val zone = ZoneId.systemDefault()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
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
    val team1Won = match.hasScore && match.scoreFt1!! > match.scoreFt2!!
    val team2Won = match.hasScore && match.scoreFt2!! > match.scoreFt1!!
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = buildString {
                        append(dayFormatter.format(local))
                        append(" · ")
                        append(timeFormatter.format(local))
                        match.group?.let { append(" · $it") }
                        match.ground?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLabel(match.team1, bold = team1Won, trailingScore = match.scoreFt1)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamLabel(match.team2, bold = team2Won, trailingScore = match.scoreFt2)
                }
            }
            IconButton(onClick = { onToggleFollow(match) }) {
                if (match.isFollowed) {
                    Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.action_unfollow))
                } else {
                    Icon(Icons.Outlined.StarBorder, contentDescription = stringResource(R.string.action_follow))
                }
            }
        }
    }
}
