package com.example.yiyuezhiming.core.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.yiyuezhiming.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled || reminder.id <= 0) return
        val nextDate = nextOccurrence(reminder.date)
        val trigger = nextDate.atTime(LocalTime.of(9, 0))
        val delayMillis = Duration.between(LocalDateTime.now(), trigger).toMillis().coerceAtLeast(0)
        val dateMillis = reminder.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_ID to reminder.id,
                    ReminderWorker.KEY_TITLE to reminder.title,
                    ReminderWorker.KEY_TYPE to reminder.type,
                    ReminderWorker.KEY_DATE to dateMillis
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(reminder.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * 提醒触发后重新调度下一年。在 Worker 中调用。
     */
    fun rescheduleAfterFired(id: Long, title: String, type: String, dateMillis: Long) {
        val originalDate = java.time.Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val nextYear = originalDate.withYear(originalDate.year + 1)
        val now = LocalDate.now()
        val target = if (!nextYear.isAfter(now)) nextYear.plusYears(1) else nextYear
        val trigger = target.atTime(LocalTime.of(9, 0))
        val delayMillis = Duration.between(LocalDateTime.now(), trigger).toMillis().coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_ID to id,
                    ReminderWorker.KEY_TITLE to title,
                    ReminderWorker.KEY_TYPE to type,
                    ReminderWorker.KEY_DATE to dateMillis
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
    }

    private fun nextOccurrence(date: LocalDate): LocalDate {
        val today = LocalDate.now()
        val thisYear = date.withYear(today.year)
        return if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
    }

    private fun workName(id: Long) = "date-reminder-$id"
}
