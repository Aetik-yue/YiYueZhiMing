package com.example.yiyuezhiming.domain.reader

enum class ContentSourceType { LOCAL_FILE, REMOTE_SOURCE, MANUAL_INPUT }

interface ContentSource {
    val type: ContentSourceType
    suspend fun search(query: String, page: Int): Result<List<SearchResult>>
    suspend fun getChapterList(bookId: String): Result<List<ChapterInfo>>
    suspend fun getChapterContent(chapterUrl: String): Result<String>
    suspend fun getBookMetadata(bookId: String): Result<BookMetadata>
}

data class SearchResult(
    val sourceId: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String?,
    val sourceType: ContentSourceType
)

data class ChapterInfo(
    val index: Int,
    val title: String,
    val url: String
)

data class BookMetadata(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?
)
