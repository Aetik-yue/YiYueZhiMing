package com.example.yiyuezhiming.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.yiyuezhiming.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            REMINDER_CHANNEL,
            "重要日期提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "提醒你们之间温柔又重要的日子"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showReminder(id: Long, title: String, type: String) {
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_heart)
            .setContentTitle("今天是$title")
            .setContentText("$type 到啦，记得准备一点小小的浪漫。")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(id.coerceAtLeast(1).toInt(), notification)
    }

    companion object {
        const val REMINDER_CHANNEL = "date_reminders"
    }
}
