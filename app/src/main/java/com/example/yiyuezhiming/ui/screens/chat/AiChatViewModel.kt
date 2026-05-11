package com.example.yiyuezhiming.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.AiChatRepository
import com.example.yiyuezhiming.data.deepseek.DeepSeekResult
import com.example.yiyuezhiming.model.AiAssistant
import com.example.yiyuezhiming.model.AiChatMessage
import com.example.yiyuezhiming.model.AiChatSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val assistants: List<AiAssistant> = emptyList(),
    val sessions: List<AiChatSession> = emptyList(),
    val activeAssistantId: Long = 0,
    val activeSessionId: Long = 0,
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null
) {
    val activeAssistant: AiAssistant?
        get() = assistants.firstOrNull { it.id == activeAssistantId }

    val assistantName: String
        get() = activeAssistant?.name ?: "小睿睿"
}

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val repository: AiChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()
    private var sessionJob: Job? = null
    private var messageJob: Job? = null

    init {
        viewModelScope.launch {
            val assistantId = repository.ensureAssistant()
            val sessionId = repository.ensureSession(assistantId)
            _uiState.update { it.copy(activeAssistantId = assistantId, activeSessionId = sessionId) }
            observeSessions(assistantId)
            observeMessages(sessionId)
        }
        viewModelScope.launch {
            repository.observeAssistants().collect { assistants ->
                _uiState.update { it.copy(assistants = assistants) }
            }
        }
    }

    fun setInput(value: String) = _uiState.update { it.copy(input = value.take(1_000), error = null) }

    fun createSession() {
        val assistantId = _uiState.value.activeAssistantId
        if (assistantId <= 0) return
        viewModelScope.launch {
            switchSession(repository.createSession(assistantId))
        }
    }

    fun switchAssistant(assistantId: Long) {
        if (assistantId == _uiState.value.activeAssistantId) return
        viewModelScope.launch {
            repository.saveActiveAssistant(assistantId)
            val sessionId = repository.ensureSession(assistantId)
            _uiState.update {
                it.copy(
                    activeAssistantId = assistantId,
                    activeSessionId = sessionId,
                    input = "",
                    error = null
                )
            }
            observeSessions(assistantId)
            observeMessages(sessionId)
        }
    }

    fun switchSession(sessionId: Long) {
        val assistantId = _uiState.value.activeAssistantId
        if (sessionId == _uiState.value.activeSessionId || assistantId <= 0) return
        repository.saveActiveSession(assistantId, sessionId)
        _uiState.update { it.copy(activeSessionId = sessionId, input = "", error = null) }
        observeMessages(sessionId)
    }

    fun saveAssistantConfig(assistant: AiAssistant?, name: String, icon: String, prompt: String, webSearchEnabled: Boolean) {
        viewModelScope.launch {
            val id = if (assistant == null) {
                repository.createAssistant(name, icon, prompt, webSearchEnabled)
            } else {
                repository.saveAssistantConfig(assistant, name, icon, prompt, webSearchEnabled)
            }
            switchAssistant(id)
        }
    }

    fun deleteAssistant(assistant: AiAssistant) {
        if (assistant.isPreset) return
        viewModelScope.launch {
            val nextAssistantId = repository.deleteAssistant(assistant.id)
            switchAssistant(nextAssistantId)
        }
    }

    fun send() {
        val content = _uiState.value.input.trim()
        val assistantId = _uiState.value.activeAssistantId
        val sessionId = _uiState.value.activeSessionId
        if (content.isBlank() || assistantId <= 0 || sessionId <= 0 || _uiState.value.isSending) return
        _uiState.update { it.copy(input = "", isSending = true, error = null) }
        viewModelScope.launch {
            val result = repository.send(sessionId, assistantId, content)
            _uiState.update {
                it.copy(
                    isSending = false,
                    error = (result as? DeepSeekResult.Failure)?.error?.userMessage
                )
            }
        }
    }

    fun clear() {
        val sessionId = _uiState.value.activeSessionId
        if (sessionId <= 0) return
        viewModelScope.launch { repository.clear(sessionId) }
    }

    fun deleteSession(sessionId: Long = _uiState.value.activeSessionId) {
        val assistantId = _uiState.value.activeAssistantId
        if (assistantId <= 0 || sessionId <= 0) return
        viewModelScope.launch {
            val nextSessionId = repository.deleteSession(assistantId, sessionId)
            _uiState.update { it.copy(activeSessionId = nextSessionId, input = "", error = null) }
            observeMessages(nextSessionId)
        }
    }

    private fun observeSessions(assistantId: Long) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            repository.observeSessions(assistantId).collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
            }
        }
    }

    private fun observeMessages(sessionId: Long) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            repository.observeMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }
}
