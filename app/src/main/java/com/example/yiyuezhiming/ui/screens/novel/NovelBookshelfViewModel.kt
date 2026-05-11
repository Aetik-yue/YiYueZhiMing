package com.example.yiyuezhiming.ui.screens.novel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yiyuezhiming.data.BookRepository
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
    val message: String? = null
)

@HiltViewModel
class NovelBookshelfViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(BookshelfSort.LAST_READ)
    private val _state = MutableStateFlow(NovelBookshelfState())
    val state: StateFlow<NovelBookshelfState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeBooks(), query, sort) { books, q, s ->
                val filtered = books.filter { book ->
                    q.isBlank() || book.title.contains(q, ignoreCase = true) || book.author.contains(q, ignoreCase = true)
                }
                val sorted = when (s) {
                    BookshelfSort.LAST_READ -> filtered.sortedByDescending { it.lastReadAt ?: it.addedAt }
                    BookshelfSort.ADDED -> filtered.sortedByDescending { it.addedAt }
                    BookshelfSort.TITLE -> filtered.sortedBy { it.title }
                }
                NovelBookshelfState(books = sorted, query = q, sort = s)
            }.collect { _state.value = it }
        }
    }

    fun setQuery(value: String) {
        query.value = value.take(40)
    }

    fun setSort(value: BookshelfSort) {
        sort.value = value
    }

    fun import(uri: Uri, name: String?) {
        if (!name.orEmpty().endsWith(".epub", ignoreCase = true)) {
            _state.update { it.copy(message = "目前只支持导入 EPUB 文件") }
            return
        }
        repository.enqueueImport(uri, name)
        _state.update { it.copy(message = "已开始导入，稍等一下就会出现在书架里") }
    }

    fun rename(book: Book, title: String) {
        viewModelScope.launch { repository.renameBook(book.id, title) }
    }

    fun delete(book: Book) {
        viewModelScope.launch { repository.deleteBook(book.id) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
