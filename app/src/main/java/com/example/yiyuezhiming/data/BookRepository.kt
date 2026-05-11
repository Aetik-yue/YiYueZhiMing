package com.example.yiyuezhiming.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.yiyuezhiming.core.worker.BookImportWorker
import com.example.yiyuezhiming.data.local.BookDao
import com.example.yiyuezhiming.data.local.BookEntity
import com.example.yiyuezhiming.data.local.ChapterEntity
import com.example.yiyuezhiming.data.reader.EpubParser
import com.example.yiyuezhiming.data.reader.PaginationEngine
import com.example.yiyuezhiming.data.reader.PageJsonCodec
import com.example.yiyuezhiming.model.Book
import com.example.yiyuezhiming.model.Chapter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class BookRepository @Inject constructor(
    private val dao: BookDao,
    private val epubParser: EpubParser,
    @ApplicationContext private val context: Context
) {
    fun observeBooks(): Flow<List<Book>> = dao.observeBooks().map { rows -> rows.map { it.toModel() } }

    fun observeBook(bookId: String): Flow<Book?> = dao.observeBook(bookId).map { it?.toModel() }

    fun observeChapters(bookId: String): Flow<List<Chapter>> =
        dao.observeChapters(bookId).map { rows -> rows.map { it.toModel() } }

    suspend fun getBook(bookId: String): Book? = dao.getBook(bookId)?.toModel()

    suspend fun getChapters(bookId: String): List<Chapter> = dao.getChapters(bookId).map { it.toModel() }

    fun enqueueImport(uri: Uri, displayName: String?) {
        val request = OneTimeWorkRequestBuilder<BookImportWorker>()
            .setInputData(
                Data.Builder()
                    .putString(BookImportWorker.KEY_URI, uri.toString())
                    .putString(BookImportWorker.KEY_NAME, displayName.orEmpty())
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "import_book_${System.currentTimeMillis()}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    suspend fun importEpub(uri: Uri, displayName: String?) = withContext(Dispatchers.IO) {
        val name = displayName?.takeIf { it.isNotBlank() } ?: "import.epub"
        if (!name.endsWith(".epub", ignoreCase = true)) error("目前只支持导入 EPUB 文件")
        val bookId = UUID.randomUUID().toString()
        val bookDir = File(context.filesDir, "books/$bookId").apply { mkdirs() }
        val epubFile = File(bookDir, safeName(name))
        context.contentResolver.openInputStream(uri)?.use { input ->
            epubFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("无法读取所选文件")
        val now = System.currentTimeMillis()
        dao.upsertBook(
            BookEntity(
                id = bookId,
                title = name.removeSuffix(".epub"),
                author = "解析中",
                coverPath = null,
                filePath = epubFile.absolutePath,
                sourceType = "LOCAL_FILE",
                sourceUrl = null,
                totalChapters = 0,
                fileSize = epubFile.length(),
                status = "IMPORTING",
                addedAt = now,
                lastReadAt = null
            )
        )
        try {
            val metadata = epubFile.inputStream().use(epubParser::parse)
            val coverPath = metadata.coverImage?.let { bytes ->
                File(bookDir, "cover").also { it.writeBytes(bytes) }.absolutePath
            }
            val paginator = PaginationEngine()
            val chapters = metadata.chapters.mapIndexed { index, epubChapter ->
                val pages = paginator.paginate(epubChapter.content, chapterIndex = index)
                ChapterEntity(
                    id = "$bookId-$index",
                    bookId = bookId,
                    chapterIndex = index,
                    title = epubChapter.title,
                    pagesJson = PageJsonCodec.encode(pages),
                    rawContent = epubChapter.content
                )
            }
            dao.upsertChapters(chapters)
            dao.upsertBook(
                BookEntity(
                    id = bookId,
                    title = metadata.title,
                    author = metadata.author,
                    coverPath = coverPath,
                    filePath = epubFile.absolutePath,
                    sourceType = "LOCAL_FILE",
                    sourceUrl = null,
                    totalChapters = chapters.size,
                    fileSize = epubFile.length(),
                    status = "READY",
                    addedAt = now,
                    lastReadAt = null
                )
            )
        } catch (error: Throwable) {
            dao.upsertBook(
                BookEntity(
                    id = bookId,
                    title = name.removeSuffix(".epub"),
                    author = "未知作者",
                    coverPath = null,
                    filePath = epubFile.absolutePath,
                    sourceType = "LOCAL_FILE",
                    sourceUrl = null,
                    totalChapters = 0,
                    fileSize = epubFile.length(),
                    status = "FAILED",
                    errorMessage = error.message ?: "EPUB 解析失败",
                    addedAt = now,
                    lastReadAt = null
                )
            )
        }
    }

    suspend fun updateProgress(bookId: String, chapterIndex: Int, pageIndex: Int) {
        dao.updateProgress(bookId, chapterIndex, pageIndex, System.currentTimeMillis())
    }

    suspend fun renameBook(bookId: String, title: String) {
        if (title.isNotBlank()) dao.renameBook(bookId, title.trim())
    }

    suspend fun deleteBook(bookId: String) {
        dao.getBook(bookId)?.filePath?.let { path ->
            File(path).parentFile?.deleteRecursively()
        }
        dao.deleteBook(bookId)
    }

    private fun safeName(value: String): String = value.replace(Regex("[\\\\/:*?\"<>|]"), "_")
}
