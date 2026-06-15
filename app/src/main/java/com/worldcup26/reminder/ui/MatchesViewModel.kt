package com.worldcup26.reminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.worldcup26.reminder.calendar.CalendarInfo
import com.worldcup26.reminder.data.MatchRepository
import com.worldcup26.reminder.data.settings.SettingsRepository
import com.worldcup26.reminder.domain.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Active list filters. All-default means "show everything". */
data class MatchFilter(
    val query: String = "",
    val group: String? = null,
    val onlyFollowed: Boolean = false,
    val onlyUpcoming: Boolean = false,
)

class MatchesViewModel(
    private val repository: MatchRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val allMatches: StateFlow<List<Match>> = repository.observeMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _filter = MutableStateFlow(MatchFilter())
    val filter: StateFlow<MatchFilter> = _filter.asStateFlow()

    /** Group labels present in the schedule, e.g. ["Group A", ... "Group L"]. */
    val groups: StateFlow<List<String>> = allMatches
        .map { matches -> matches.mapNotNull { it.group }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val matches: StateFlow<List<Match>> =
        combine(allMatches, _filter) { matches, f -> matches.applyFilter(f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    // --- Settings ---

    val defaultReminderMinutes: StateFlow<Int> = settings.defaultReminderMinutes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsRepository.DEFAULT_REMINDER_MINUTES,
        )

    val selectedCalendarId: StateFlow<Long?> = settings.selectedCalendarId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())
    val calendars: StateFlow<List<CalendarInfo>> = _calendars.asStateFlow()

    // --- Actions ---

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            runCatching { repository.refresh() }
            _refreshing.value = false
        }
    }

    fun setQuery(query: String) { _filter.value = _filter.value.copy(query = query) }
    fun setGroup(group: String?) { _filter.value = _filter.value.copy(group = group) }
    fun toggleOnlyFollowed() {
        _filter.value = _filter.value.copy(onlyFollowed = !_filter.value.onlyFollowed)
    }
    fun toggleOnlyUpcoming() {
        _filter.value = _filter.value.copy(onlyUpcoming = !_filter.value.onlyUpcoming)
    }

    fun toggleFollow(match: Match) {
        viewModelScope.launch {
            if (match.isFollowed) {
                repository.unfollow(match.id)
            } else {
                repository.follow(
                    matchId = match.id,
                    reminderMinutesBefore = settings.defaultReminderMinutes.first(),
                    calendarId = settings.selectedCalendarId.first(),
                )
            }
        }
    }

    fun setDefaultReminderMinutes(minutes: Int) {
        viewModelScope.launch { settings.setDefaultReminderMinutes(minutes) }
    }

    fun setSelectedCalendarId(id: Long?) {
        viewModelScope.launch { settings.setSelectedCalendarId(id) }
    }

    fun loadCalendars() {
        viewModelScope.launch {
            _calendars.value = withContext(Dispatchers.IO) { repository.availableCalendars() }
        }
    }

    private fun List<Match>.applyFilter(f: MatchFilter): List<Match> = filter { match ->
        (f.group == null || match.group == f.group) &&
            (!f.onlyFollowed || match.isFollowed) &&
            (!f.onlyUpcoming || match.isUpcoming) &&
            (f.query.isBlank() ||
                match.team1.contains(f.query, ignoreCase = true) ||
                match.team2.contains(f.query, ignoreCase = true))
    }

    class Factory(
        private val repository: MatchRepository,
        private val settings: SettingsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MatchesViewModel(repository, settings) as T
    }
}
