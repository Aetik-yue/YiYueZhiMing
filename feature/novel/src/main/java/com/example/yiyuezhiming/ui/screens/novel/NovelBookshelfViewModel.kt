package com.example.yiyuezhiming.ui.screens.novel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.BookRepository
import com.example.yiyuezhiming.data.ImportEnqueueResult
import com.example.yiyuezhiming.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BookshelfSort { LAST_READ, ADDED, TITLE }

data class NovelBookshelfState(
    val books: List<Book> = emptyList(),
    val query: String = "",
    val sort: BookshelfSort = BookshelfSort.LAST_READ,
    val defaultSourceUrl: String = "",
    val message: String? = null
)

@HiltViewModel
class NovelBookshelfViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(BookshelfSort.LAST_READ)
    private val defaultSourceUrl = MutableStateFlow(repository.defaultSourceUrl())
    private val _state = MutableStateFlow(NovelBookshelfState())
    val state: StateFlow<NovelBookshelfState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeBooks(), query, sort, defaultSourceUrl) { books, q, s, defaultUrl ->
                val filtered = books.filter { book ->
                    q.isBlank() || book.title.contains(q, ignoreCase = true) || book.author.contains(q, ignoreCase = true)
                }
                val sorted = when (s) {
                    BookshelfSort.LAST_READ -> filtered.sortedByDescending { it.lastReadAt ?: it.addedAt }
                    BookshelfSort.ADDED -> filtered.sortedByDescending { it.addedAt }
                    BookshelfSort.TITLE -> filtered.sortedBy { it.title }
                }
                NovelBookshelfState(
                    books = sorted,
                    query = q,
                    sort = s,
                    defaultSourceUrl = defaultUrl,
                    message = _state.value.message
                )
            }.collect { _state.value = it }
        }
    }

    fun setQuery(value: String) {
        query.value = value.take(40)
    }

    fun setSort(value: BookshelfSort) {
        sort.value = value
    }

    fun import(uri: Uri, name: String?, mimeType: String?) {
        viewModelScope.launch {
            when (val result = repository.enqueueImport(uri, name, mimeType)) {
                ImportEnqueueResult.Success ->
                    _state.update { it.copy(message = "已开始导入，完成后会在书架显示结果") }
                is ImportEnqueueResult.Failure ->
                    _state.update { it.copy(message = "导入失败：${result.reason}") }
            }
        }
    }

    fun fetchOnline(url: String, title: String?) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _state.update { it.copy(message = "请输入 http 或 https 开头的网址") }
            return
        }
        repository.enqueueRemoteImport(url, title)
        _state.update { it.copy(message = "已开始在线抓取，完成后会自动加入书架") }
    }

    fun rename(book: Book, title: String) {
        viewModelScope.launch { repository.renameBook(book.id, title) }
    }

    fun saveDefaultSourceUrl(url: String) {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            _state.update { it.copy(message = "请输入 http 或 https 开头的默认源地址") }
            return
        }
        repository.saveDefaultSourceUrl(trimmed)
        defaultSourceUrl.value = trimmed
        _state.update { it.copy(message = "已保存为默认抓取源") }
    }

    fun clearDefaultSourceUrl() {
        repository.clearDefaultSourceUrl()
        defaultSourceUrl.value = ""
        _state.update { it.copy(message = "已清除默认抓取源") }
    }

    fun delete(book: Book) {
        viewModelScope.launch { repository.deleteBook(book.id) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
