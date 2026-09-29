package com.alcorte.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_NAME = "AlCorteDailyReminderWork"

    fun scheduleDailyReminder(context: Context) {
        val now = LocalDateTime.now()
        var targetTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(9, 0))
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }

        val initialDelayMinutes = Duration.between(now, targetTime).toMinutes()

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }
}
