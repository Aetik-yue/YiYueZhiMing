package com.example.yiyuezhiming.ui.screens.fortune

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.FortuneRepository
import com.example.yiyuezhiming.data.deepseek.DeepSeekResult
import com.example.yiyuezhiming.model.FortuneRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FortuneUiState(
    val today: FortuneRecord? = null,
    val history: List<FortuneRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FortuneViewModel @Inject constructor(
    private val repository: FortuneRepository
) : ViewModel() {
    private val signState = MutableStateFlow(FortuneUiState())
    private val tarotState = MutableStateFlow(FortuneUiState())

    fun state(type: String): StateFlow<FortuneUiState> =
        if (type == FortuneRepository.TYPE_SIGN) signState.asStateFlow() else tarotState.asStateFlow()

    fun load(type: String) {
        val holder = holder(type)
        viewModelScope.launch {
            holder.update { it.copy(today = repository.getToday(type)) }
        }
        viewModelScope.launch {
            repository.observeHistory(type).collect { history ->
                holder.update { it.copy(history = history, today = history.firstOrNull()) }
            }
        }
    }

    fun draw(type: String) {
        val holder = holder(type)
        if (holder.value.isLoading) return
        viewModelScope.launch {
            holder.update { it.copy(isLoading = true, error = null) }
            val result = if (type == FortuneRepository.TYPE_SIGN) {
                repository.drawDailySign()
            } else {
                repository.drawTarot()
            }
            holder.update {
                when (result) {
                    is DeepSeekResult.Success -> it.copy(today = result.value, isLoading = false)
                    is DeepSeekResult.Failure -> it.copy(isLoading = false, error = result.error.userMessage)
                }
            }
        }
    }

    private fun holder(type: String): MutableStateFlow<FortuneUiState> =
        if (type == FortuneRepository.TYPE_SIGN) signState else tarotState
}

