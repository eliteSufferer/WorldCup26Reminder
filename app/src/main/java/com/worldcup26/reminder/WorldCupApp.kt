package com.worldcup26.reminder

import android.app.Application
import com.worldcup26.reminder.data.AppContainer
import com.worldcup26.reminder.notify.Notifications
import com.worldcup26.reminder.work.ScheduleRefreshWorker

/**
 * Process-wide entry point.
 *
 * Owns the [AppContainer] (manual dependency graph — no DI framework needed for
 * an app this size), creates the notification channel, and enqueues the daily
 * schedule refresh.
 */
class WorldCupApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.ensureChannel(this)
        ScheduleRefreshWorker.enqueuePeriodic(this)
    }
}
