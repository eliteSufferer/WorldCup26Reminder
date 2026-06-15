package com.worldcup26.reminder.data

import android.content.Context
import com.worldcup26.reminder.calendar.CalendarWriter
import com.worldcup26.reminder.data.local.AppDatabase
import com.worldcup26.reminder.data.remote.BroadcastsApi
import com.worldcup26.reminder.data.remote.ScheduleApi
import com.worldcup26.reminder.data.settings.SettingsRepository
import com.worldcup26.reminder.work.AlarmScheduler

/**
 * Hand-rolled dependency graph. Small enough that a DI framework would be overkill;
 * everything is lazily built and shared process-wide via [WorldCupApp].
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val api by lazy { ScheduleApi() }
    private val broadcastsApi by lazy { BroadcastsApi(appContext) }
    private val database by lazy { AppDatabase.get(appContext) }
    private val alarmScheduler by lazy { AlarmScheduler(appContext) }
    private val calendarWriter by lazy { CalendarWriter(appContext) }

    val settingsRepository by lazy { SettingsRepository(appContext) }

    val matchRepository by lazy {
        MatchRepository(
            api = api,
            broadcasts = broadcastsApi,
            dao = database.matchDao(),
            alarms = alarmScheduler,
            calendar = calendarWriter,
        )
    }
}
