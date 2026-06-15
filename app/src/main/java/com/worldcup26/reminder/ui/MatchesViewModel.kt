package com.worldcup26.reminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.worldcup26.reminder.calendar.CalendarInfo
import com.worldcup26.reminder.data.MatchRepository
import com.worldcup26.reminder.data.settings.SettingsRepository
import com.worldcup26.reminder.domain.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One-shot status messages surfaced as a snackbar. */
sealed interface UiEvent {
    /** Match followed; [addedToCalendar] tells whether the calendar write succeeded. */
    data class Saved(val addedToCalendar: Boolean) : UiEvent
    data object Removed : UiEvent
    data class Refreshed(val count: Int) : UiEvent
    data object RefreshFailed : UiEvent
}

/** Active list filters for the Groups tab. */
data class MatchFilter(
    val query: String = "",
    val group: String? = null,
    val onlyFollowed: Boolean = false,
    /** Past (finished) matches are hidden unless this is on. */
    val showPrevious: Boolean = false,
)

class MatchesViewModel(
    private val repository: MatchRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val allMatches: StateFlow<List<Match>> = repository.observeMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _filter = MutableStateFlow(MatchFilter())
    val filter: StateFlow<MatchFilter> = _filter.asStateFlow()

    /** Group labels present in the schedule, e.g. ["Group A", ... "Group L"]. */
    val groups: StateFlow<List<String>> = allMatches
        .map { matches -> matches.mapNotNull { it.group }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Group-stage matches after applying the filter bar. */
    val groupMatches: StateFlow<List<Match>> =
        combine(allMatches, _filter) { matches, f ->
            matches.filter { !it.isKnockout }.applyFilter(f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** All knockout matches (the bracket always shows the full structure). */
    val knockoutMatches: StateFlow<List<Match>> = allMatches
        .map { matches -> matches.filter { it.isKnockout }.sortedBy { it.kickoff } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // --- Settings ---

    val defaultReminderMinutes: StateFlow<Int> = settings.defaultReminderMinutes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsRepository.DEFAULT_REMINDER_MINUTES,
        )

    val selectedCalendarId: StateFlow<Long?> = settings.selectedCalendarId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val defaultTabIndex: StateFlow<Int> = settings.defaultTabIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsRepository.TAB_GROUPS)

    private val _calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())
    val calendars: StateFlow<List<CalendarInfo>> = _calendars.asStateFlow()

    // --- Actions ---

    /** [silent] suppresses the snackbar — used for the automatic first-launch load. */
    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            _refreshing.value = true
            runCatching { repository.refresh() }
                .onSuccess { if (!silent) _events.send(UiEvent.Refreshed(it)) }
                .onFailure { if (!silent) _events.send(UiEvent.RefreshFailed) }
            _refreshing.value = false
        }
    }

    fun setQuery(query: String) { _filter.value = _filter.value.copy(query = query) }
    fun setGroup(group: String?) { _filter.value = _filter.value.copy(group = group) }
    fun toggleOnlyFollowed() {
        _filter.value = _filter.value.copy(onlyFollowed = !_filter.value.onlyFollowed)
    }
    fun toggleShowPrevious() {
        _filter.value = _filter.value.copy(showPrevious = !_filter.value.showPrevious)
    }

    fun toggleFollow(match: Match) {
        viewModelScope.launch {
            if (match.isFollowed) {
                repository.unfollow(match.id)
                _events.send(UiEvent.Removed)
            } else {
                val addedToCalendar = repository.follow(
                    matchId = match.id,
                    reminderMinutesBefore = settings.defaultReminderMinutes.first(),
                    calendarId = settings.selectedCalendarId.first(),
                )
                _events.send(UiEvent.Saved(addedToCalendar))
            }
        }
    }

    fun setDefaultReminderMinutes(minutes: Int) {
        viewModelScope.launch { settings.setDefaultReminderMinutes(minutes) }
    }

    fun setSelectedCalendarId(id: Long?) {
        viewModelScope.launch { settings.setSelectedCalendarId(id) }
    }

    fun setDefaultTabIndex(index: Int) {
        viewModelScope.launch { settings.setDefaultTabIndex(index) }
    }

    fun loadCalendars() {
        viewModelScope.launch {
            _calendars.value = withContext(Dispatchers.IO) { repository.availableCalendars() }
        }
    }

    private fun List<Match>.applyFilter(f: MatchFilter): List<Match> = filter { match ->
        (f.group == null || match.group == f.group) &&
            (!f.onlyFollowed || match.isFollowed) &&
            (f.showPrevious || !match.isFinished) &&
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
