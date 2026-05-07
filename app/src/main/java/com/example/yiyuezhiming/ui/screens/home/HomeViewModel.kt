package com.example.yiyuezhiming.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.MemoryRepository
import com.example.yiyuezhiming.data.MockData
import com.example.yiyuezhiming.model.Memory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val memories: List<Memory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedIfEmpty(MockData.memories)
            repository.observeMemories()
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "记忆加载失败")
                    }
                }
                .collect { memories ->
                    _uiState.value = HomeUiState(memories = memories, isLoading = false)
                }
        }
    }
}
