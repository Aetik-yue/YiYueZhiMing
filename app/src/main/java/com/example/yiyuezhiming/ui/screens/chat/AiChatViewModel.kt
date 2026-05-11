package com.example.yiyuezhiming.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.AiChatRepository
import com.example.yiyuezhiming.data.deepseek.DeepSeekResult
import com.example.yiyuezhiming.model.AiChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val repository: AiChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun setInput(value: String) = _uiState.update { it.copy(input = value.take(1_000), error = null) }

    fun send() {
        val content = _uiState.value.input.trim()
        if (content.isBlank() || _uiState.value.isSending) return
        _uiState.update { it.copy(input = "", isSending = true, error = null) }
        viewModelScope.launch {
            val result = repository.send(content)
            _uiState.update {
                it.copy(
                    isSending = false,
                    error = (result as? DeepSeekResult.Failure)?.error?.userMessage
                )
            }
        }
    }

    fun clear() {
        viewModelScope.launch { repository.clear() }
    }
}

