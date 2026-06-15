package com.worldcup26.reminder.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.worldcup26.reminder.WorldCupApp
import java.util.concurrent.TimeUnit

/**
 * Daily background refresh of the schedule JSON. The fixture list is essentially
 * static, so once a day is plenty — it mainly catches kickoff-time tweaks and
 * knockout placeholders being resolved to real teams.
 */
class ScheduleRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = (applicationContext as WorldCupApp).container.matchRepository
        return runCatching { repo.refresh() }
            .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
    }

    companion object {
        private const val WORK_NAME = "schedule_refresh_daily"

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<ScheduleRefreshWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
