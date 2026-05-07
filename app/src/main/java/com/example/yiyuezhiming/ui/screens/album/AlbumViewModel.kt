package com.example.yiyuezhiming.ui.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.yiyuezhiming.data.MemoryRepository
import com.example.yiyuezhiming.model.Memory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val filter: String = "全部",
    val memories: List<Memory> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
) {
    val filtered: List<Memory>
        get() = when (filter) {
            "全部", "最近7天", "本月" -> memories
            else -> memories.filter { it.category == filter }
        }
}

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePhotoMemories()
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "相册加载失败") }
                }
                .collect { memories ->
                    _uiState.update { it.copy(isLoading = false, memories = memories) }
                }
        }
    }

    fun setFilter(value: String) = _uiState.update { it.copy(filter = value) }

    fun importPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, successMessage = null) }
            try {
                val count = repository.importPhotos(uris)
                _uiState.update {
                    it.copy(
                        filter = "导入",
                        isImporting = false,
                        successMessage = "已导入 $count 张照片"
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = error.message ?: "导入照片失败，请稍后重试"
                    )
                }
            }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(successMessage = null) }
}
