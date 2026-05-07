package com.example.yiyuezhiming.ui.screens.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.MemoRepository
import com.example.yiyuezhiming.model.Memo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoUiState(
    val search: String = "",
    val selectedCategory: String = "全部",
    val categories: List<String> = emptyList(),
    val memos: List<Memo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filtered: List<Memo>
        get() {
            val keyword = search.trim()
            return memos.filter { memo ->
                val matchesCategory = selectedCategory == "全部" || memo.category == selectedCategory
                val matchesSearch = keyword.isEmpty() ||
                    memo.title.contains(keyword, ignoreCase = true) ||
                    memo.content.contains(keyword, ignoreCase = true)
                matchesCategory && matchesSearch
            }.sortedWith(
                compareByDescending<Memo> { it.isPinned }
                    .thenBy { it.isDone }
                    .thenByDescending { it.updatedAt }
            )
        }
}

@HiltViewModel
class MemoViewModel @Inject constructor(
    private val repository: MemoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoUiState())
    val uiState: StateFlow<MemoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaults()
            combine(repository.observeCategories(), repository.observeMemos()) { categories, memos ->
                categories to memos
            }.catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "备忘录加载失败") }
            }.collect { (categories, memos) ->
                _uiState.update { current ->
                    current.copy(
                        categories = categories,
                        memos = memos,
                        selectedCategory = if (current.selectedCategory == "全部" || current.selectedCategory in categories) {
                            current.selectedCategory
                        } else {
                            "全部"
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSearch(value: String) = _uiState.update { it.copy(search = value) }

    fun setCategory(value: String) = _uiState.update { it.copy(selectedCategory = value) }

    fun addCategory(value: String) {
        val name = value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.ensureCategory(name)
            _uiState.update { it.copy(selectedCategory = name) }
        }
    }

    fun saveMemo(memo: Memo) {
        viewModelScope.launch {
            repository.save(memo)
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            repository.delete(memo.id)
        }
    }

    fun togglePinned(memo: Memo) {
        viewModelScope.launch {
            repository.setPinned(memo.id, !memo.isPinned)
        }
    }

    fun toggleDone(memo: Memo) {
        viewModelScope.launch {
            repository.setDone(memo.id, !memo.isDone)
        }
    }
}

