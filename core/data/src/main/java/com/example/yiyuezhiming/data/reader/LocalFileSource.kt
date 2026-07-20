package com.example.yiyuezhiming.data.reader

import com.example.yiyuezhiming.domain.reader.BookMetadata
import com.example.yiyuezhiming.domain.reader.ChapterInfo
import com.example.yiyuezhiming.domain.reader.ContentSource
import com.example.yiyuezhiming.domain.reader.ContentSourceType
import com.example.yiyuezhiming.domain.reader.SearchResult
import javax.inject.Inject

class LocalFileSource @Inject constructor() : ContentSource {
    override val type: ContentSourceType = ContentSourceType.LOCAL_FILE

    override suspend fun search(query: String, page: Int): Result<List<SearchResult>> = Result.success(emptyList())

    override suspend fun getChapterList(bookId: String): Result<List<ChapterInfo>> =
        Result.failure(UnsupportedOperationException("本地文件章节由导入流程解析"))

    override suspend fun getChapterContent(chapterUrl: String): Result<String> =
        Result.failure(UnsupportedOperationException("本地文件内容由导入流程解析"))

    override suspend fun getBookMetadata(bookId: String): Result<BookMetadata> =
        Result.failure(UnsupportedOperationException("本地文件元数据由导入流程解析"))
}
