package com.worldcup26.reminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.worldcup26.reminder.data.MatchRepository
import com.worldcup26.reminder.domain.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MatchesViewModel(private val repository: MatchRepository) : ViewModel() {

    val matches: StateFlow<List<Match>> = repository.observeMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            runCatching { repository.refresh() }
            _refreshing.value = false
        }
    }

    fun toggleFollow(match: Match, reminderMinutesBefore: Int = 30) {
        viewModelScope.launch {
            if (match.isFollowed) {
                repository.unfollow(match.id)
            } else {
                repository.follow(match.id, reminderMinutesBefore)
            }
        }
    }

    class Factory(private val repository: MatchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MatchesViewModel(repository) as T
    }
}
