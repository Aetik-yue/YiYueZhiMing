package com.example.yiyuezhiming.ui.screens.fortune

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.FortuneRepository
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
    val isLoading: Boolean = false
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
                // 仅更新 history，不覆盖 today（today 应由 getToday() 正确反映当日记录）
                holder.update { it.copy(history = history) }
            }
        }
    }

    fun draw(type: String) {
        val holder = holder(type)
        // 将 guard 移入协程内，避免 TOCTOU 竞争
        if (holder.value.isLoading) return
        viewModelScope.launch {
            if (holder.value.isLoading) return@launch
            holder.update { it.copy(isLoading = true) }
            val record = if (type == FortuneRepository.TYPE_SIGN) {
                repository.drawDailySign()
            } else {
                repository.drawTarot()
            }
            holder.update { it.copy(today = record, isLoading = false) }
        }
    }

    private fun holder(type: String): MutableStateFlow<FortuneUiState> =
        if (type == FortuneRepository.TYPE_SIGN) signState else tarotState
}
