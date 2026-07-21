package com.example.yiyuezhiming.core.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.yiyuezhiming.core.notification.NotificationHelper
import com.example.yiyuezhiming.core.worker.ReminderScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val reminderScheduler: ReminderScheduler
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (!canNotify()) return Result.success()
        val id = inputData.getLong(KEY_ID, 0)
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val type = inputData.getString(KEY_TYPE).orEmpty()
        val dateMillis = inputData.getLong(KEY_DATE, 0L)
        if (id <= 0 || title.isBlank()) return Result.failure()
        notificationHelper.showReminder(id, title, type)
        // 重新调度下一年的提醒
        reminderScheduler.rescheduleAfterFired(id, title, type, dateMillis)
        return Result.success()
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val KEY_ID = "id"
        const val KEY_TITLE = "title"
        const val KEY_TYPE = "type"
        const val KEY_DATE = "date"
    }
}
