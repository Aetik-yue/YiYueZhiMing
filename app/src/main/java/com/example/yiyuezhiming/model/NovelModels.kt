package com.example.yiyuezhiming.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverPath: String?,
    val filePath: String?,
    val sourceType: String,
    val totalChapters: Int,
    val currentChapterIndex: Int,
    val currentPageInChapter: Int,
    val fileSize: Long,
    val status: String,
    val errorMessage: String?,
    val addedAt: Long,
    val lastReadAt: Long?
) {
    val progress: Float
        get() = if (totalChapters <= 0) 0f else (currentChapterIndex + 1).coerceAtMost(totalChapters).toFloat() / totalChapters
}

data class Chapter(
    val id: String,
    val bookId: String,
    val index: Int,
    val title: String,
    val pages: List<Page>,
    val rawContent: String?
)

data class Page(
    val chapterIndex: Int,
    val pageIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val lines: List<Line>
)

data class Line(
    val text: String,
    val startOffset: Int,
    val endOffset: Int
)

enum class ReadMode { PAGED, SCROLL }

enum class ReaderTheme { DAY, NIGHT, EYE }

data class ReaderSettings(
    val fontSizeSp: Float = 20f,
    val lineSpacing: Float = 8f,
    val pagePadding: Float = 24f,
    val theme: ReaderTheme = ReaderTheme.EYE,
    val readMode: ReadMode = ReadMode.PAGED
)

data class Bookmark(
    val id: String,
    val bookId: String,
    val chapterIndex: Int,
    val pageIndex: Int,
    val excerpt: String?,
    val note: String?,
    val createdAt: Long
)
