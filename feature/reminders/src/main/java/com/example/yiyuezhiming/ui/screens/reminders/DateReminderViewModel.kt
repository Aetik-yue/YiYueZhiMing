package com.example.yiyuezhiming.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.MockData
import com.example.yiyuezhiming.data.ReminderRepository
import com.example.yiyuezhiming.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DateReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val editingReminder: Reminder? = null,
    val title: String = "",
    val type: String = "纪念日",
    val dateText: String = LocalDate.now().toString(),
    val enabled: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DateReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DateReminderUiState())
    val uiState: StateFlow<DateReminderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedIfEmpty(MockData.reminders)
            repository.observeReminders()
                .catch { error -> _uiState.update { it.copy(error = error.message ?: "提醒加载失败") } }
                .collect { reminders -> _uiState.update { it.copy(reminders = reminders) } }
        }
    }

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value, error = null) }
    fun onTypeChanged(value: String) = _uiState.update { it.copy(type = value) }
    fun onDateTextChanged(value: String) = _uiState.update { it.copy(dateText = value, error = null) }
    fun onEnabledChanged(value: Boolean) = _uiState.update { it.copy(enabled = value) }

    fun startAdd() = _uiState.update {
        it.copy(
            editingReminder = null,
            title = "",
            type = "纪念日",
            dateText = LocalDate.now().toString(),
            enabled = true,
            error = null
        )
    }

    fun startEdit(reminder: Reminder) = _uiState.update {
        it.copy(
            editingReminder = reminder,
            title = reminder.title,
            type = reminder.type,
            dateText = reminder.date.toString(),
            enabled = reminder.isEnabled,
            error = null
        )
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder.id)
        }
    }

    fun addReminder(onSaved: () -> Unit) {
        val state = _uiState.value
        val date = runCatching { LocalDate.parse(state.dateText) }.getOrNull()
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "请写下这个日子的名字") }
            return
        }
        if (date == null) {
            _uiState.update { it.copy(error = "日期格式应为 yyyy-MM-dd") }
            return
        }
        viewModelScope.launch {
            val editing = state.editingReminder
            repository.saveReminder(
                Reminder(
                    id = editing?.id ?: 0,
                    title = state.title.trim(),
                    type = state.type,
                    date = date,
                    isEnabled = state.enabled,
                    createdAt = editing?.createdAt ?: System.currentTimeMillis()
                )
            )
            _uiState.update {
                it.copy(
                    editingReminder = null,
                    title = "",
                    type = "纪念日",
                    dateText = LocalDate.now().toString(),
                    enabled = true,
                    error = null
                )
            }
            onSaved()
        }
    }
}
