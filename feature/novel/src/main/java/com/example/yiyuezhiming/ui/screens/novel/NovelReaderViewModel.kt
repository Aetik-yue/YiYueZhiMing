package com.example.yiyuezhiming.ui.screens.novel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.BookRepository
import com.example.yiyuezhiming.data.local.ChapterMeta
import com.example.yiyuezhiming.data.reader.PaginationEngine
import com.example.yiyuezhiming.model.Book
import com.example.yiyuezhiming.model.Page
import com.example.yiyuezhiming.model.ReaderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NovelReaderState(
    val book: Book? = null,
    val chapterMetas: List<ChapterMeta> = emptyList(),
    val chapterIndex: Int = 0,
    val pageIndex: Int = 0,
    val pages: List<Page> = emptyList(),
    val chapterTitle: String = "",
    val settings: ReaderSettings = ReaderSettings(),
    val menuVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val pagesCount: Int get() = pages.size
    val totalChapters: Int get() = chapterMetas.size
    val progressText: String
        get() = "${chapterIndex + 1}/${totalChapters.coerceAtLeast(1)} · ${pageIndex + 1}/${pagesCount.coerceAtLeast(1)}"
    val overallProgress: Float
        get() {
            if (totalChapters == 0) return 0f
            val chapterPart = if (pagesCount > 0) pageIndex.toFloat() / pagesCount else 0f
            return (chapterIndex + chapterPart) / totalChapters
        }
}

@HiltViewModel
class NovelReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository
) : ViewModel() {
    private val bookId: String = checkNotNull(savedStateHandle["bookId"])
    private val _state = MutableStateFlow(NovelReaderState())
    val state: StateFlow<NovelReaderState> = _state.asStateFlow()
    private var paginateJob: Job? = null
    private var progressRestored = false

    init {
        val savedSettings = repository.loadReaderSettings()
        _state.update { it.copy(settings = savedSettings) }
        viewModelScope.launch {
            repository.observeBook(bookId).collect { book ->
                if (book == null) {
                    _state.update { it.copy(isLoading = false, error = "找不到这本书") }
                } else {
                    _state.update { it.copy(book = book, isLoading = false) }
                    maybeRestoreProgress(book)
                }
            }
        }
        viewModelScope.launch {
            repository.observeChapterMetas(bookId).collect { metas ->
                _state.update { it.copy(chapterMetas = metas) }
                _state.value.book?.let { maybeRestoreProgress(it) }
            }
        }
    }

    // 等 book 与 chapterMetas 都到齐后，用持久化的章节/页内位置恢复一次；
    // 之后任何 Flow 的后续发射都不再覆盖已恢复的位置。
    private fun maybeRestoreProgress(book: Book) {
        if (progressRestored) return
        val metas = _state.value.chapterMetas
        if (metas.isEmpty()) return
        progressRestored = true
        val chapterIndex = book.currentChapterIndex.coerceIn(0, (metas.size - 1).coerceAtLeast(0))
        val pageIndex = book.currentPageInChapter.coerceAtLeast(0)
        _state.update { it.copy(chapterIndex = chapterIndex) }
        loadChapter(chapterIndex, pageIndex)
    }

    fun toggleMenu() = _state.update { it.copy(menuVisible = !it.menuVisible) }

    fun goTo(chapterIndex: Int, pageIndex: Int = 0) {
        val metas = _state.value.chapterMetas
        if (chapterIndex !in metas.indices) return
        _state.update { it.copy(chapterIndex = chapterIndex, menuVisible = false) }
        loadChapter(chapterIndex, pageIndex)
        saveProgress(chapterIndex, pageIndex)
    }

    fun nextPage() {
        val s = _state.value
        when {
            s.pageIndex < s.pagesCount - 1 -> goToPage(s.pageIndex + 1)
            s.chapterIndex < s.totalChapters - 1 -> goTo(s.chapterIndex + 1, 0)
        }
    }

    fun previousPage() {
        val s = _state.value
        when {
            s.pageIndex > 0 -> goToPage(s.pageIndex - 1)
            s.chapterIndex > 0 -> {
                goTo(s.chapterIndex - 1, Int.MAX_VALUE)
            }
        }
    }

    fun goToPage(pageIndex: Int) {
        val s = _state.value
        val target = pageIndex.coerceIn(0, (s.pagesCount - 1).coerceAtLeast(0))
        _state.update { it.copy(pageIndex = target) }
        saveProgress(s.chapterIndex, target)
    }

    fun updateSettings(settings: ReaderSettings) {
        _state.update { it.copy(settings = settings) }
        repository.saveReaderSettings(settings)
        repository.invalidatePageCountCache(bookId)
        repaginate()
    }

    private fun loadChapter(chapterIndex: Int, targetPage: Int) {
        paginateJob?.cancel()
        paginateJob = viewModelScope.launch {
            val content = repository.getChapterContent(bookId, chapterIndex)
            if (content.isNullOrBlank()) {
                _state.update { it.copy(pages = emptyList(), pageIndex = 0, chapterTitle = "") }
                return@launch
            }
            val meta = _state.value.chapterMetas.getOrNull(chapterIndex)
            val settings = _state.value.settings
            val pages = paginate(content, settings)
            val resolvedPage = if (targetPage == Int.MAX_VALUE) {
                (pages.size - 1).coerceAtLeast(0)
            } else {
                targetPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
            }
            _state.update {
                it.copy(
                    pages = pages,
                    pageIndex = resolvedPage,
                    chapterTitle = meta?.title.orEmpty(),
                    error = null
                )
            }
            saveProgress(chapterIndex, resolvedPage)
        }
    }

    private fun repaginate() {
        val s = _state.value
        paginateJob?.cancel()
        paginateJob = viewModelScope.launch {
            val content = repository.getChapterContent(bookId, s.chapterIndex) ?: return@launch
            val currentOffset = s.pages.getOrNull(s.pageIndex)?.startOffset ?: 0
            val pages = paginate(content, s.settings)
            val newPage = pages.indexOfLast { it.startOffset <= currentOffset }.coerceAtLeast(0)
            _state.update { it.copy(pages = pages, pageIndex = newPage) }
            saveProgress(s.chapterIndex, newPage)
        }
    }

    private suspend fun paginate(content: String, settings: ReaderSettings): List<Page> =
        withContext(Dispatchers.Default) {
            val density = 3f
            val engine = PaginationEngine(
                pageWidth = (360f - settings.pagePadding * 2f) * density,
                pageHeight = 640f * density,
                lineSpacing = settings.lineSpacing * density,
                paragraphSpacing = 16f * density
            )
            engine.paginate(content, chapterIndex = _state.value.chapterIndex, fontSize = settings.fontSizeSp * density)
        }

    private fun saveProgress(chapterIndex: Int, pageIndex: Int) {
        val pagesInChapter = _state.value.pages.size
        viewModelScope.launch { repository.updateProgress(bookId, chapterIndex, pageIndex, pagesInChapter) }
    }
}
