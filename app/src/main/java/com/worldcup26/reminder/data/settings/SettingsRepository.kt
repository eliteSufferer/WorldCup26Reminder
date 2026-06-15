package com.worldcup26.reminder.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** User preferences persisted with DataStore. */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val defaultReminderMinutes: Flow<Int> =
        store.data.map { it[KEY_REMINDER_MINUTES] ?: DEFAULT_REMINDER_MINUTES }

    /** Calendar id chosen by the user, or null to fall back to the primary calendar. */
    val selectedCalendarId: Flow<Long?> =
        store.data.map { it[KEY_CALENDAR_ID] }

    suspend fun setDefaultReminderMinutes(minutes: Int) {
        store.edit { it[KEY_REMINDER_MINUTES] = minutes }
    }

    suspend fun setSelectedCalendarId(id: Long?) {
        store.edit {
            if (id == null) it.remove(KEY_CALENDAR_ID) else it[KEY_CALENDAR_ID] = id
        }
    }

    companion object {
        const val DEFAULT_REMINDER_MINUTES = 30
        val REMINDER_OPTIONS = listOf(10, 15, 30, 60, 120)

        private val KEY_REMINDER_MINUTES = intPreferencesKey("default_reminder_minutes")
        private val KEY_CALENDAR_ID = longPreferencesKey("selected_calendar_id")
    }
}
