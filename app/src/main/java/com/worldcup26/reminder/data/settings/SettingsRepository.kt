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

    /** Tab shown on launch: 0 = Groups, 1 = Playoff. */
    val defaultTabIndex: Flow<Int> =
        store.data.map { it[KEY_DEFAULT_TAB] ?: TAB_GROUPS }

    suspend fun setDefaultReminderMinutes(minutes: Int) {
        store.edit { it[KEY_REMINDER_MINUTES] = minutes }
    }

    suspend fun setDefaultTabIndex(index: Int) {
        store.edit { it[KEY_DEFAULT_TAB] = index }
    }

    suspend fun setSelectedCalendarId(id: Long?) {
        store.edit {
            if (id == null) it.remove(KEY_CALENDAR_ID) else it[KEY_CALENDAR_ID] = id
        }
    }

    companion object {
        const val DEFAULT_REMINDER_MINUTES = 30
        val REMINDER_OPTIONS = listOf(10, 15, 30, 60, 120)

        const val TAB_GROUPS = 0
        const val TAB_PLAYOFF = 1

        private val KEY_REMINDER_MINUTES = intPreferencesKey("default_reminder_minutes")
        private val KEY_CALENDAR_ID = longPreferencesKey("selected_calendar_id")
        private val KEY_DEFAULT_TAB = intPreferencesKey("default_tab_index")
    }
}
