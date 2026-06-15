package com.worldcup26.reminder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.worldcup26.reminder.R
import com.worldcup26.reminder.calendar.CalendarInfo
import com.worldcup26.reminder.data.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    reminderMinutes: Int,
    reminderOptions: List<Int>,
    onReminderSelect: (Int) -> Unit,
    defaultTabIndex: Int,
    onDefaultTabSelect: (Int) -> Unit,
    calendars: List<CalendarInfo>,
    selectedCalendarId: Long?,
    onCalendarSelect: (Long?) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(stringResource(R.string.settings_reminder_header))
            reminderOptions.forEach { minutes ->
                SelectableRow(
                    label = stringResource(R.string.settings_reminder_minutes, minutes),
                    selected = minutes == reminderMinutes,
                    onClick = { onReminderSelect(minutes) },
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            SectionHeader(stringResource(R.string.settings_tab_header))
            SelectableRow(
                label = stringResource(R.string.settings_tab_groups),
                selected = defaultTabIndex == SettingsRepository.TAB_GROUPS,
                onClick = { onDefaultTabSelect(SettingsRepository.TAB_GROUPS) },
            )
            SelectableRow(
                label = stringResource(R.string.settings_tab_playoff),
                selected = defaultTabIndex == SettingsRepository.TAB_PLAYOFF,
                onClick = { onDefaultTabSelect(SettingsRepository.TAB_PLAYOFF) },
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            SectionHeader(stringResource(R.string.settings_calendar_header))
            if (calendars.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_calendar_none),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else {
                SelectableRow(
                    label = stringResource(R.string.settings_calendar_default),
                    selected = selectedCalendarId == null,
                    onClick = { onCalendarSelect(null) },
                )
                calendars.forEach { cal ->
                    SelectableRow(
                        label = if (cal.accountName.isBlank()) cal.displayName
                        else "${cal.displayName} · ${cal.accountName}",
                        selected = selectedCalendarId == cal.id,
                        onClick = { onCalendarSelect(cal.id) },
                    )
                }
                Text(
                    text = stringResource(R.string.settings_calendar_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
