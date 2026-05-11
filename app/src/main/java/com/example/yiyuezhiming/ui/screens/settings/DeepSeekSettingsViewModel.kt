package com.example.yiyuezhiming.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.yiyuezhiming.data.deepseek.DeepSeekRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DeepSeekSettingsState(
    val hasKey: Boolean = false,
    val maskedKey: String = ""
)

@HiltViewModel
class DeepSeekSettingsViewModel @Inject constructor(
    private val repository: DeepSeekRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        DeepSeekSettingsState(
            hasKey = repository.hasApiKey(),
            maskedKey = repository.currentApiKeyMask()
        )
    )
    val state: StateFlow<DeepSeekSettingsState> = _state.asStateFlow()

    fun saveKey(value: String) {
        repository.saveApiKey(value)
        refresh()
    }

    fun clearKey() {
        repository.clearApiKey()
        refresh()
    }

    private fun refresh() {
        _state.update {
            DeepSeekSettingsState(
                hasKey = repository.hasApiKey(),
                maskedKey = repository.currentApiKeyMask()
            )
        }
    }
}

