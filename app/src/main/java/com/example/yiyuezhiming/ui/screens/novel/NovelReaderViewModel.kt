package com.example.yiyuezhiming.ui.screens.novel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.BookRepository
import com.example.yiyuezhiming.model.Book
import com.example.yiyuezhiming.model.Chapter
import com.example.yiyuezhiming.model.ReaderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NovelReaderState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val chapterIndex: Int = 0,
    val pageIndex: Int = 0,
    val settings: ReaderSettings = ReaderSettings(),
    val menuVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentChapter: Chapter? = chapters.getOrNull(chapterIndex)
    val pagesCount: Int = currentChapter?.pages?.size ?: 0
    val progressText: String
        get() = "${chapterIndex + 1}/${chapters.size.coerceAtLeast(1)} · ${pageIndex + 1}/${pagesCount.coerceAtLeast(1)}"
}

@HiltViewModel
class NovelReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository
) : ViewModel() {
    private val bookId: String = checkNotNull(savedStateHandle["bookId"])
    private val _state = MutableStateFlow(NovelReaderState())
    val state: StateFlow<NovelReaderState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeBook(bookId), repository.observeChapters(bookId)) { book, chapters ->
                if (book == null) {
                    NovelReaderState(isLoading = false, error = "找不到这本书")
                } else {
                    NovelReaderState(
                        book = book,
                        chapters = chapters,
                        chapterIndex = book.currentChapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0)),
                        pageIndex = book.currentPageInChapter,
                        isLoading = false
                    )
                }
            }.collect { state ->
                _state.value = state.copy(
                    pageIndex = state.pageIndex.coerceIn(0, ((state.currentChapter?.pages?.size ?: 1) - 1).coerceAtLeast(0))
                )
            }
        }
    }

    fun toggleMenu() = _state.update { it.copy(menuVisible = !it.menuVisible) }

    fun goTo(chapterIndex: Int, pageIndex: Int = 0) {
        val state = _state.value
        val chapter = state.chapters.getOrNull(chapterIndex) ?: return
        val targetPage = pageIndex.coerceIn(0, (chapter.pages.size - 1).coerceAtLeast(0))
        _state.update { it.copy(chapterIndex = chapterIndex, pageIndex = targetPage, menuVisible = false) }
        saveProgress(chapterIndex, targetPage)
    }

    fun nextPage() {
        val state = _state.value
        val pageCount = state.currentChapter?.pages?.size ?: return
        when {
            state.pageIndex < pageCount - 1 -> goTo(state.chapterIndex, state.pageIndex + 1)
            state.chapterIndex < state.chapters.lastIndex -> goTo(state.chapterIndex + 1, 0)
        }
    }

    fun previousPage() {
        val state = _state.value
        when {
            state.pageIndex > 0 -> goTo(state.chapterIndex, state.pageIndex - 1)
            state.chapterIndex > 0 -> {
                val previous = state.chapters[state.chapterIndex - 1]
                goTo(state.chapterIndex - 1, (previous.pages.size - 1).coerceAtLeast(0))
            }
        }
    }

    fun updateSettings(settings: ReaderSettings) {
        _state.update { it.copy(settings = settings) }
    }

    private fun saveProgress(chapterIndex: Int, pageIndex: Int) {
        viewModelScope.launch { repository.updateProgress(bookId, chapterIndex, pageIndex) }
    }
}
