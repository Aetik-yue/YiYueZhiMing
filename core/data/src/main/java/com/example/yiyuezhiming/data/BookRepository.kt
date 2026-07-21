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
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Result of enqueuing a local file import. Lets the UI know if the file could be staged. */
sealed interface ImportEnqueueResult {
    object Success : ImportEnqueueResult
    data class Failure(val reason: String) : ImportEnqueueResult
}

@Singleton
class BookRepository @Inject constructor(
    private val dao: BookDao,
    private val epubParser: EpubParser,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    private val readerPrefs = context.getSharedPreferences("novel_reader", Context.MODE_PRIVATE)

    fun observeBooks(): Flow<List<Book>> = dao.observeBooks().map { rows -> rows.map { it.toModel() } }

    fun observeBook(bookId: String): Flow<Book?> = dao.observeBook(bookId).map { it?.toModel() }

    fun observeChapters(bookId: String): Flow<List<Chapter>> =
        dao.observeChapters(bookId).map { rows -> rows.map { it.toModel() } }

    suspend fun getBook(bookId: String): Book? = dao.getBook(bookId)?.toModel()

    suspend fun getChapters(bookId: String): List<Chapter> = dao.getChapters(bookId).map { it.toModel() }

    fun observeChapterMetas(bookId: String): Flow<List<com.example.yiyuezhiming.data.local.ChapterMeta>> =
        dao.observeChapterMetas(bookId)

    suspend fun getChapterContent(bookId: String, chapterIndex: Int): String? =
        dao.getChapterContent(bookId, chapterIndex)

    fun loadReaderSettings(): com.example.yiyuezhiming.model.ReaderSettings {
        val theme = runCatching {
            com.example.yiyuezhiming.model.ReaderTheme.valueOf(readerPrefs.getString("theme", "EYE") ?: "EYE")
        }.getOrDefault(com.example.yiyuezhiming.model.ReaderTheme.EYE)
        return com.example.yiyuezhiming.model.ReaderSettings(
            fontSizeSp = readerPrefs.getFloat("fontSize", 20f),
            lineSpacing = readerPrefs.getFloat("lineSpacing", 8f),
            pagePadding = readerPrefs.getFloat("pagePadding", 24f),
            theme = theme
        )
    }

    fun saveReaderSettings(settings: com.example.yiyuezhiming.model.ReaderSettings) {
        readerPrefs.edit()
            .putFloat("fontSize", settings.fontSizeSp)
            .putFloat("lineSpacing", settings.lineSpacing)
            .putFloat("pagePadding", settings.pagePadding)
            .putString("theme", settings.theme.name)
            .apply()
    }

    suspend fun enqueueImport(uri: Uri, displayName: String?, mimeType: String?): ImportEnqueueResult =
        withContext(Dispatchers.IO) {
            val name = displayName?.takeIf { it.isNotBlank() } ?: "import_${System.currentTimeMillis()}.txt"
            val stagingDir = File(context.filesDir, "books/staging").apply { mkdirs() }
            val stagingFile = File(stagingDir, "${System.currentTimeMillis()}_${safeName(name)}")
            val copyResult = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    stagingFile.outputStream().use { output -> input.copyTo(output) }
                }
                stagingFile.exists() && stagingFile.length() > 0
            }
            if (copyResult.getOrDefault(false).not()) {
                stagingFile.delete()
                val reason = copyResult.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                    ?: if (stagingFile.length() == 0L) "文件为空或无法读取" else "无法读取所选文件，请重试"
                return@withContext ImportEnqueueResult.Failure(reason)
            }
            val request = OneTimeWorkRequestBuilder<BookImportWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(BookImportWorker.KEY_LOCAL_PATH, stagingFile.absolutePath)
                        .putString(BookImportWorker.KEY_NAME, name)
                        .putString(BookImportWorker.KEY_MIME, mimeType.orEmpty())
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "import_book_${System.currentTimeMillis()}",
                ExistingWorkPolicy.KEEP,
                request
            )
            ImportEnqueueResult.Success
        }

    fun enqueueRemoteImport(url: String, title: String?) {
        val request = OneTimeWorkRequestBuilder<BookImportWorker>()
            .setInputData(
                Data.Builder()
                    .putString(BookImportWorker.KEY_URL, url)
                    .putString(BookImportWorker.KEY_NAME, title.orEmpty())
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "fetch_book_${System.currentTimeMillis()}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun defaultSourceUrl(): String = readerPrefs.getString(KEY_DEFAULT_SOURCE_URL, "").orEmpty()

    fun saveDefaultSourceUrl(url: String) {
        readerPrefs.edit().putString(KEY_DEFAULT_SOURCE_URL, url.trim()).apply()
    }

    fun clearDefaultSourceUrl() {
        readerPrefs.edit().remove(KEY_DEFAULT_SOURCE_URL).apply()
    }

    suspend fun importBook(localPath: String, displayName: String?, mimeType: String?) = withContext(Dispatchers.IO) {
        try {
            val localFile = File(localPath)
            if (!localFile.exists()) error("文件不存在")
            val kind = resolveImportKind(localFile.name, displayName, mimeType)
            when (kind) {
                ImportKind.EPUB -> importEpubInternal(localFile, displayName?.takeIf { it.isNotBlank() } ?: localFile.name)
                ImportKind.TXT -> importTxtInternal(localFile, displayName?.takeIf { it.isNotBlank() } ?: localFile.name)
            }
        } catch (error: Throwable) {
            createFailedBook(
                title = displayName?.substringBeforeLast('.', "本地导入").takeUnless { it.isNullOrBlank() } ?: "本地导入",
                author = "导入失败",
                sourceType = "LOCAL_FILE",
                filePath = null,
                sourceUrl = null,
                message = error.message ?: "导入失败"
            )
        } finally {
            File(localPath).delete()
        }
    }

    suspend fun importRemoteNovel(url: String, titleHint: String?) = withContext(Dispatchers.IO) {
        try {
            val remote = fetchRemoteNovel(url)
            importTxtContent(
                title = titleHint?.takeIf { it.isNotBlank() } ?: remote.title,
                author = "在线抓取",
                content = remote.content,
                sourceType = "REMOTE_SOURCE",
                sourceUrl = url
            )
        } catch (error: Throwable) {
            createFailedBook(
                title = titleHint?.takeIf { it.isNotBlank() } ?: "在线抓取",
                author = "抓取失败",
                sourceType = "REMOTE_SOURCE",
                filePath = null,
                sourceUrl = url,
                message = error.message ?: "在线抓取失败"
            )
        }
    }

    private suspend fun importEpubInternal(sourceFile: File, name: String) {
        if (!name.endsWith(".epub", ignoreCase = true)) error("目前只支持导入 EPUB 文件")
        val bookId = UUID.randomUUID().toString()
        val bookDir = File(context.filesDir, "books/$bookId").apply { mkdirs() }
        val epubFile = File(bookDir, safeName(name))
        sourceFile.copyTo(epubFile, overwrite = true)
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
            val chapterPageCounts = mutableListOf<Int>()
            val chapters = metadata.chapters.mapIndexed { index, epubChapter ->
                val pages = paginator.paginate(epubChapter.content, chapterIndex = index)
                chapterPageCounts.add(pages.size)
                ChapterEntity(
                    id = "$bookId-$index",
                    bookId = bookId,
                    chapterIndex = index,
                    title = epubChapter.title,
                    pagesJson = PageJsonCodec.encode(pages),
                    rawContent = epubChapter.content
                )
            }
            val totalPages = chapterPageCounts.sum()
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
                    totalPages = totalPages,
                    currentPageInBook = 0,
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

    private suspend fun importTxtInternal(sourceFile: File, name: String) {
        val bookId = UUID.randomUUID().toString()
        val bookDir = File(context.filesDir, "books/$bookId").apply { mkdirs() }
        val txtFile = File(bookDir, safeName(name))
        sourceFile.copyTo(txtFile, overwrite = true)
        val title = name.substringBeforeLast('.', name)
        val now = System.currentTimeMillis()
        dao.upsertBook(
            BookEntity(
                id = bookId,
                title = title,
                author = "本地 TXT",
                coverPath = null,
                filePath = txtFile.absolutePath,
                sourceType = "LOCAL_FILE",
                sourceUrl = null,
                totalChapters = 0,
                fileSize = txtFile.length(),
                status = "IMPORTING",
                addedAt = now,
                lastReadAt = null
            )
        )
        try {
            finishTxtImport(
                bookId = bookId,
                title = title,
                author = "本地 TXT",
                filePath = txtFile.absolutePath,
                sourceType = "LOCAL_FILE",
                sourceUrl = null,
                fileSize = txtFile.length(),
                addedAt = now,
                text = readTextCompat(txtFile.readBytes())
            )
        } catch (error: Throwable) {
            dao.upsertBook(
                BookEntity(
                    id = bookId,
                    title = title,
                    author = "本地 TXT",
                    coverPath = null,
                    filePath = txtFile.absolutePath,
                    sourceType = "LOCAL_FILE",
                    sourceUrl = null,
                    totalChapters = 0,
                    fileSize = txtFile.length(),
                    status = "FAILED",
                    errorMessage = error.message ?: "TXT 解析失败",
                    addedAt = now,
                    lastReadAt = null
                )
            )
        }
    }

    private suspend fun importTxtContent(title: String, author: String, content: String, sourceType: String, sourceUrl: String?) {
        val bookId = UUID.randomUUID().toString()
        val bookDir = File(context.filesDir, "books/$bookId").apply { mkdirs() }
        val txtFile = File(bookDir, "${safeName(title)}.txt")
        txtFile.writeText(content, Charsets.UTF_8)
        val now = System.currentTimeMillis()
        dao.upsertBook(
            BookEntity(
                id = bookId,
                title = title,
                author = author,
                coverPath = null,
                filePath = txtFile.absolutePath,
                sourceType = sourceType,
                sourceUrl = sourceUrl,
                totalChapters = 0,
                fileSize = txtFile.length(),
                status = "IMPORTING",
                addedAt = now,
                lastReadAt = null
            )
        )
        finishTxtImport(bookId, title, author, txtFile.absolutePath, sourceType, sourceUrl, txtFile.length(), now, content)
    }

    private suspend fun finishTxtImport(
        bookId: String,
        title: String,
        author: String,
        filePath: String?,
        sourceType: String,
        sourceUrl: String?,
        fileSize: Long,
        addedAt: Long,
        text: String
    ) {
        val normalized = normalizeTxt(text)
        if (normalized.isBlank()) error("TXT 文件没有可阅读内容")
        val paginator = PaginationEngine()
        val chapterPageCounts = mutableListOf<Int>()
        val chapters = splitTxtChapters(normalized, title).mapIndexed { index, chapter ->
            val pages = paginator.paginate(chapter.content, chapterIndex = index)
            chapterPageCounts.add(pages.size)
            ChapterEntity(
                id = "$bookId-$index",
                bookId = bookId,
                chapterIndex = index,
                title = chapter.title,
                pagesJson = PageJsonCodec.encode(pages),
                rawContent = chapter.content
            )
        }
        val totalPages = chapterPageCounts.sum()
        dao.upsertChapters(chapters)
        dao.upsertBook(
            BookEntity(
                id = bookId,
                title = title,
                author = author,
                coverPath = null,
                filePath = filePath,
                sourceType = sourceType,
                sourceUrl = sourceUrl,
                totalChapters = chapters.size,
                totalPages = totalPages,
                currentPageInBook = 0,
                fileSize = fileSize,
                status = "READY",
                addedAt = addedAt,
                lastReadAt = null
            )
        )
    }

    suspend fun updateProgress(bookId: String, chapterIndex: Int, pageIndex: Int, pagesInChapter: Int) {
        val counts = ensureChapterPageCounts(bookId)
        if (chapterIndex in counts.indices) counts[chapterIndex] = pagesInChapter
        val currentPageInBook = counts.take(chapterIndex).sum() + pageIndex
        val totalPages = counts.sum()
        dao.updateProgress(bookId, chapterIndex, pageIndex, currentPageInBook, totalPages, System.currentTimeMillis())
    }

    private val chapterPageCountCache = mutableMapOf<String, IntArray>()

    private suspend fun ensureChapterPageCounts(bookId: String): IntArray {
        chapterPageCountCache[bookId]?.let { return it }
        val chapters = dao.getChapters(bookId)
        val counts = IntArray(chapters.size) { i ->
            com.example.yiyuezhiming.data.reader.PageJsonCodec.decode(chapters[i].pagesJson).size
        }
        chapterPageCountCache[bookId] = counts
        return counts
    }

    /** 字号/行距/边距等设置变更后调用，使缓存失效 */
    fun invalidatePageCountCache(bookId: String) {
        chapterPageCountCache.remove(bookId)
    }

    suspend fun renameBook(bookId: String, title: String) {
        if (title.isNotBlank()) dao.renameBook(bookId, title.trim())
    }

    suspend fun deleteBook(bookId: String) {
        chapterPageCountCache.remove(bookId)
        dao.getBook(bookId)?.filePath?.let { path ->
            File(path).parentFile?.deleteRecursively()
        }
        dao.deleteBook(bookId)
    }

    private fun readTextCompat(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }
        val charsets = listOf(Charsets.UTF_8, Charset.forName("GB18030"), Charsets.UTF_16LE, Charsets.UTF_16BE)
        charsets.forEach { charset ->
            val decoded = runCatching {
                charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString()
            }.getOrNull()
            if (!decoded.isNullOrBlank()) return decoded
        }
        return Charset.forName("GB18030")
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    }

    private suspend fun createFailedBook(
        title: String,
        author: String,
        sourceType: String,
        filePath: String?,
        sourceUrl: String?,
        message: String
    ) {
        val now = System.currentTimeMillis()
        dao.upsertBook(
            BookEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                author = author,
                coverPath = null,
                filePath = filePath,
                sourceType = sourceType,
                sourceUrl = sourceUrl,
                totalChapters = 0,
                fileSize = filePath?.let { File(it).length() } ?: 0,
                status = "FAILED",
                errorMessage = message,
                addedAt = now,
                lastReadAt = null
            )
        )
    }

    private fun resolveImportKind(fileName: String, displayName: String?, mimeType: String?): ImportKind {
        val name = (displayName ?: fileName).lowercase()
        val type = mimeType.orEmpty().lowercase()
        return when {
            name.endsWith(".epub") || type == "application/epub+zip" -> ImportKind.EPUB
            else -> ImportKind.TXT
        }
    }

    private suspend fun fetchRemoteNovel(url: String): RemoteNovel {
        coroutineContext.ensureActive()
        val firstHtml = fetchText(url)
        val chapterLinks = extractChapterLinks(url, firstHtml)
        if (chapterLinks.size >= 2) {
            val chapters = chapterLinks.take(60).mapIndexed { index, link ->
                coroutineContext.ensureActive()
                val html = fetchText(link.url)
                val title = link.title.ifBlank { extractHtmlTitle(html).ifBlank { "第 ${index + 1} 章" } }
                val content = extractReadableText(html)
                if (content.isBlank()) error("章节正文为空：$title")
                "$title\n\n$content"
            }
            return RemoteNovel(
                title = extractHtmlTitle(firstHtml).ifBlank { chapterLinks.first().title.ifBlank { "在线小说" } },
                content = chapters.joinToString("\n\n")
            )
        }
        val title = extractHtmlTitle(firstHtml).ifBlank { "在线小说" }
        val content = extractReadableText(firstHtml)
        if (content.isBlank()) error("没有抓取到可阅读正文")
        return RemoteNovel(title, "$title\n\n$content")
    }

    private fun fetchText(url: String): String {
        if (!url.startsWith("http://") && !url.startsWith("https://")) error("请输入 http 或 https 地址")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "YiYueZhiMingReader/1.0")
            .get()
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("网页请求失败：${response.code}")
            return response.body?.string().orEmpty()
        }
    }

    private fun extractChapterLinks(baseUrl: String, html: String): List<ChapterLink> {
        val base = java.net.URL(baseUrl)
        val regex = Regex("""(?is)<a\s+[^>]*href=["']([^"']+)["'][^>]*>(.*?)</a>""")
        return regex.findAll(html).mapNotNull { match ->
            val title = cleanHtml(match.groupValues[2]).trim()
            if (!title.contains(Regex("""第.{1,20}[章节回卷部篇]|chapter\s*\d+""", RegexOption.IGNORE_CASE))) return@mapNotNull null
            val raw = match.groupValues[1]
            val url = runCatching { java.net.URL(base, raw).toString() }.getOrNull() ?: return@mapNotNull null
            ChapterLink(title.take(60), url)
        }.distinctBy { it.url }.toList()
    }

    private fun extractHtmlTitle(html: String): String =
        Regex("""(?is)<h1[^>]*>(.*?)</h1>""").find(html)?.groupValues?.getOrNull(1)?.let(::cleanHtml)?.takeIf { it.isNotBlank() }
            ?: Regex("""(?is)<title[^>]*>(.*?)</title>""").find(html)?.groupValues?.getOrNull(1)?.let(::cleanHtml).orEmpty()

    private fun extractReadableText(html: String): String {
        val body = Regex("""(?is)<body[^>]*>(.*?)</body>""").find(html)?.groupValues?.getOrNull(1) ?: html
        val paragraphs = Regex("""(?is)<p[^>]*>(.*?)</p>""").findAll(body).map { cleanHtml(it.groupValues[1]) }.filter { it.length >= 2 }.toList()
        if (paragraphs.size >= 3) return paragraphs.joinToString("\n\n")
        return cleanHtml(
            body.replace(Regex("(?i)<br\\s*/?>"), "\n")
                .replace(Regex("(?i)</div>|</section>|</article>"), "\n")
        )
    }

    private fun cleanHtml(value: String): String = value
        .replace(Regex("(?is)<(script|style).*?</\\1>"), "")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("[ \\t]+"), " ")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()

    private fun normalizeTxt(value: String): String = value
        .removePrefix("\uFEFF")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace(Regex("[\\t ]+"), " ")
        .replace(Regex("\\n{4,}"), "\n\n\n")
        .trim()

    private fun splitTxtChapters(text: String, fallbackTitle: String): List<TxtChapter> {
        // Primary pattern: 第X章/节/回/卷/部/篇
        val primaryRegex = Regex("""(?m)^\s*(第.{1,20}[章节回卷部篇].*)$""")
        // Secondary patterns for other common chapter formats
        val secondaryRegexes = listOf(
            // English: Chapter N / CHAPTER N
            Regex("""(?m)^\s*((?:Chapter|CHAPTER)\s+\d+.*)$"""),
            // Numbered: N. or N、 at start of line
            Regex("""(?m)^\s*(\d{1,4}[.、].*)$"""),
            // Chinese parenthesized numbers: （一） or (一)
            Regex("""(?m)^\s*([（(][一二三四五六七八九十百千零\d]+[）)].*)$"""),
            // 卷X or 卷 X at start of line
            Regex("""(?m)^\s*(卷\s*[一二三四五六七八九十百千零\d]+.*)$"""),
            // English: Part N / PART N
            Regex("""(?m)^\s*((?:Part|PART)\s+\d+.*)$""")
        )

        var matches = primaryRegex.findAll(text).toList()
        if (matches.size < 2) {
            for (regex in secondaryRegexes) {
                val secondaryMatches = regex.findAll(text).toList()
                if (secondaryMatches.size >= 2) {
                    matches = secondaryMatches
                    break
                }
            }
        }
        if (matches.size < 2) return listOf(TxtChapter(fallbackTitle, text))

        val chapters = mutableListOf<TxtChapter>()
        val preface = text.substring(0, matches.first().range.first).trim()
        if (preface.isNotBlank()) chapters += TxtChapter("序章", preface)
        matches.forEachIndexed { index, match ->
            val start = match.range.first
            val end = matches.getOrNull(index + 1)?.range?.first ?: text.length
            val title = match.groupValues[1].trim().take(40)
            val content = text.substring(start, end).trim()
            if (content.isNotBlank()) chapters += TxtChapter(title, content)
        }
        return chapters.ifEmpty { listOf(TxtChapter(fallbackTitle, text)) }
    }

    private fun safeName(value: String): String = value.replace(Regex("[\\\\/:*?\"<>|]"), "_")

    private data class TxtChapter(val title: String, val content: String)
    private data class RemoteNovel(val title: String, val content: String)
    private data class ChapterLink(val title: String, val url: String)
    private enum class ImportKind { EPUB, TXT }

    private companion object {
        const val KEY_DEFAULT_SOURCE_URL = "default_source_url"
    }
}
