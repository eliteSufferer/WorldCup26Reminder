package com.worldcup26.reminder.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.worldcup26.reminder.WorldCupApp
import com.worldcup26.reminder.data.settings.SettingsRepository
import com.worldcup26.reminder.ui.theme.WorldCupTheme

private enum class Screen { LIST, SETTINGS }

class MainActivity : ComponentActivity() {

    private val viewModel: MatchesViewModel by viewModels {
        val container = (application as WorldCupApp).container
        MatchesViewModel.Factory(container.matchRepository, container.settingsRepository)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // Calendar list depends on READ_CALENDAR being granted.
            viewModel.loadCalendars()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()

        setContent {
            WorldCupTheme {
                var screen by remember { mutableStateOf(Screen.LIST) }

                // System back on the settings screen returns to the list instead of
                // leaving the app.
                BackHandler(enabled = screen == Screen.SETTINGS) { screen = Screen.LIST }

                when (screen) {
                    Screen.LIST -> {
                        val groupMatches by viewModel.groupMatches.collectAsState()
                        val knockoutMatches by viewModel.knockoutMatches.collectAsState()
                        val groups by viewModel.groups.collectAsState()
                        val filter by viewModel.filter.collectAsState()
                        val refreshing by viewModel.refreshing.collectAsState()
                        val initialTab by viewModel.defaultTabIndex.collectAsState()
                        MatchListScreen(
                            groupMatches = groupMatches,
                            knockoutMatches = knockoutMatches,
                            groups = groups,
                            filter = filter,
                            refreshing = refreshing,
                            initialTab = initialTab,
                            events = viewModel.events,
                            onRefresh = { viewModel.refresh() },
                            onToggleFollow = viewModel::toggleFollow,
                            onQueryChange = viewModel::setQuery,
                            onGroupChange = viewModel::setGroup,
                            onToggleOnlyFollowed = viewModel::toggleOnlyFollowed,
                            onToggleShowPrevious = viewModel::toggleShowPrevious,
                            onOpenSettings = {
                                viewModel.loadCalendars()
                                screen = Screen.SETTINGS
                            },
                        )
                    }
                    Screen.SETTINGS -> {
                        val reminderMinutes by viewModel.defaultReminderMinutes.collectAsState()
                        val defaultTabIndex by viewModel.defaultTabIndex.collectAsState()
                        val calendars by viewModel.calendars.collectAsState()
                        val selectedCalendarId by viewModel.selectedCalendarId.collectAsState()
                        SettingsScreen(
                            reminderMinutes = reminderMinutes,
                            reminderOptions = SettingsRepository.REMINDER_OPTIONS,
                            onReminderSelect = viewModel::setDefaultReminderMinutes,
                            defaultTabIndex = defaultTabIndex,
                            onDefaultTabSelect = viewModel::setDefaultTabIndex,
                            calendars = calendars,
                            selectedCalendarId = selectedCalendarId,
                            onCalendarSelect = viewModel::setSelectedCalendarId,
                            onBack = { screen = Screen.LIST },
                        )
                    }
                }
            }
        }

        // Populate on first launch when the cache is still empty.
        if (viewModel.allMatches.value.isEmpty()) viewModel.refresh(silent = true)
    }

    private fun requestRuntimePermissions() {
        val perms = buildList {
            add(Manifest.permission.READ_CALENDAR)
            add(Manifest.permission.WRITE_CALENDAR)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        permissionLauncher.launch(perms)
    }
}
