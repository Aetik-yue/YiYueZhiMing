package com.example.yiyuezhiming.data

import com.example.yiyuezhiming.core.worker.ReminderScheduler
import com.example.yiyuezhiming.data.local.ReminderDao
import com.example.yiyuezhiming.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler
) {
    fun observeReminders(): Flow<List<Reminder>> =
        dao.observeReminders().map { rows -> rows.map { it.toModel() } }

    suspend fun addReminder(reminder: Reminder): Long {
        val id = dao.insert(reminder.toEntity())
        val saved = reminder.copy(id = id)
        if (saved.isEnabled) scheduler.schedule(saved) else scheduler.cancel(id)
        return id
    }

    suspend fun deleteReminder(id: Long) {
        dao.delete(id)
        scheduler.cancel(id)
    }

    suspend fun seedIfEmpty(reminders: List<Reminder>) {
        if (dao.count() == 0) {
            reminders.forEach { addReminder(it) }
        }
    }
}
