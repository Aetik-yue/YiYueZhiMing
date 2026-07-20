package com.example.yiyuezhiming.data.reader

import android.util.Xml
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import org.xmlpull.v1.XmlPullParser

data class EpubMetadata(
    val title: String,
    val author: String,
    val coverImage: ByteArray?,
    val chapters: List<EpubChapter>,
    val toc: List<TocEntry> = emptyList()
)

data class EpubChapter(
    val id: String,
    val href: String,
    val title: String,
    val content: String
)

data class TocEntry(
    val title: String,
    val href: String,
    val children: List<TocEntry> = emptyList()
)

@Singleton
class EpubParser @Inject constructor() {

    companion object {
        private const val MAX_ENTRY_SIZE = 5L * 1024 * 1024 // 5MB
        private val TEXT_EXTENSIONS = setOf(".xhtml", ".html", ".htm", ".xml", ".opf", ".ncx")
        private val IMAGE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".gif", ".svg")
    }

    fun parse(input: InputStream): EpubMetadata {
        val entries = readZip(input) { name, size ->
            val lower = name.lowercase()
            val isText = TEXT_EXTENSIONS.any { lower.endsWith(it) }
            val isImage = IMAGE_EXTENSIONS.any { lower.endsWith(it) }
            // Always include text files under the size limit
            if (isText && size <= MAX_ENTRY_SIZE) return@readZip true
            // Skip image files entirely (cover read is sacrificed for memory savings)
            if (isImage) return@readZip false
            // Skip any entry larger than 5MB that isn't a known text type
            if (size > MAX_ENTRY_SIZE) return@readZip false
            // Include other small files (e.g. css, fonts metadata)
            true
        }
        val container = entries["META-INF/container.xml"] ?: error("找不到 EPUB 入口文件")
        val opfPath = parseContainer(container)
        val opf = entries[opfPath] ?: error("找不到 EPUB 内容清单")
        val opfBase = opfPath.substringBeforeLast('/', "")
        val packageInfo = parseOpf(opf, opfBase)
        val chapters = packageInfo.spine.mapIndexedNotNull { index, idref ->
            val item = packageInfo.manifest[idref] ?: return@mapIndexedNotNull null
            val href = item.href
            val content = findEntry(entries, href) ?: return@mapIndexedNotNull null
            val xhtmlString = decodeEntry(content)
            val extractedTitle = extractTitle(xhtmlString)
            EpubChapter(
                id = item.id.ifBlank { "chapter_$index" },
                href = href,
                title = extractedTitle.ifBlank { "第 ${index + 1} 章" },
                content = cleanXhtml(xhtmlString)
            )
        }
        if (chapters.isEmpty()) error("没有解析到可阅读章节")
        val cover = packageInfo.coverHref?.let { findEntry(entries, it) }
        return EpubMetadata(
            title = packageInfo.title.ifBlank { File(opfPath).nameWithoutExtension.ifBlank { "未命名书籍" } },
            author = packageInfo.author.ifBlank { "未知作者" },
            coverImage = cover,
            chapters = chapters
        )
    }

    /**
     * Decode a chapter/resource byte array to text. Tolerant of malformed or non-UTF-8 bytes
     * (which happen in real-world EPUBs) instead of throwing like ByteArray.decodeToString().
     */
    private fun decodeEntry(bytes: ByteArray): String {
        return Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    }

    /**
     * Resolve a manifest href to its actual zip entry, tolerating:
     * - URL-encoded names (e.g. %20, Chinese file names)
     * - leading "./" prefixes
     * - case differences
     * - windows-style backslashes
     * - basename-only mismatch
     */
    private fun findEntry(entries: Map<String, ByteArray>, href: String): ByteArray? {
        val candidates = buildList {
            add(href)
            add(href.removePrefix("./"))
            add(percentDecode(href))
            add(href.lowercase())
            add(percentDecode(href).lowercase())
            add(href.replace('\\', '/'))
        }
        for (candidate in candidates) {
            entries[candidate]?.let { return it }
        }
        val base = href.substringAfterLast('/').substringAfterLast('\\')
        if (base.isNotBlank()) {
            entries.entries.firstOrNull { it.key.substringAfterLast('/').substringAfterLast('\\') == base }
                ?.value?.let { return it }
        }
        return null
    }

    /** Percent-decode %XX sequences (leaving '+' untouched, unlike java.net.URLDecoder). */
    private fun percentDecode(value: String): String {
        if ('%' !in value) return value
        val out = StringBuilder(value.length)
        var i = 0
        while (i < value.length) {
            if (value[i] == '%' && i + 2 < value.length) {
                val hex = value.substring(i + 1, i + 3)
                val code = hex.toIntOrNull(16)
                if (code != null) {
                    out.append(code.toChar())
                    i += 3
                    continue
                }
            }
            out.append(value[i])
            i++
        }
        return out.toString()
    }

    private fun readZip(input: InputStream, shouldInclude: (name: String, size: Long) -> Boolean): Map<String, ByteArray> {
        val map = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val size = entry.size.takeIf { it >= 0 } ?: Long.MAX_VALUE
                    if (shouldInclude(entry.name, size)) {
                        map[entry.name] = zip.readBytes()
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return map
    }

    private fun parseContainer(bytes: ByteArray): String {
        val parser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), null)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                return parser.getAttributeValue(null, "full-path")
            }
        }
        error("container.xml 缺少 rootfile")
    }

    private fun parseOpf(bytes: ByteArray, opfBase: String): PackageInfo {
        val parser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), null)
        val manifest = linkedMapOf<String, ManifestItem>()
        val spine = mutableListOf<String>()
        var title = ""
        var author = ""
        var coverId: String? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name.lowercase()) {
                "dc:title", "title" -> title = parser.nextTextSafe()
                "dc:creator", "creator" -> author = parser.nextTextSafe()
                "meta" -> if (parser.getAttributeValue(null, "name") == "cover") coverId = parser.getAttributeValue(null, "content")
                "item" -> {
                    val id = parser.getAttributeValue(null, "id").orEmpty()
                    val href = parser.getAttributeValue(null, "href").orEmpty()
                    val mediaType = parser.getAttributeValue(null, "media-type").orEmpty()
                    val properties = parser.getAttributeValue(null, "properties").orEmpty()
                    if (id.isNotBlank() && href.isNotBlank()) {
                        manifest[id] = ManifestItem(
                            id = id,
                            href = joinPath(opfBase, href),
                            mediaType = mediaType,
                            title = "",
                            properties = properties
                        )
                    }
                }
                "itemref" -> parser.getAttributeValue(null, "idref")?.let(spine::add)
            }
        }
        val coverHref = manifest[coverId]?.href ?: manifest.values.firstOrNull { it.properties.contains("cover-image") }?.href
        return PackageInfo(title, author, manifest, spine, coverHref)
    }

    private fun extractTitle(xhtml: String): String {
        val patterns = listOf(
            Regex("(?is)<title[^>]*>(.*?)</title>"),
            Regex("(?is)<h1[^>]*>(.*?)</h1>"),
            Regex("(?is)<h2[^>]*>(.*?)</h2>"),
            Regex("(?is)<h3[^>]*>(.*?)</h3>")
        )
        for (pattern in patterns) {
            val match = pattern.find(xhtml)
            if (match != null) {
                val text = match.groupValues[1]
                    .replace(Regex("<[^>]+>"), "")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (text.isNotBlank()) return text
            }
        }
        return ""
    }

    private fun cleanXhtml(value: String): String {
        return value
            .replace(Regex("(?is)<(script|style).*?</\\1>"), "")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>|</div>|</h[1-6]>|</li>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("&#x([0-9a-fA-F]+);")) { match ->
                match.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: match.value
            }
            .replace(Regex("&#(\\d+);")) { match ->
                match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
            }
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun XmlPullParser.nextTextSafe(): String =
        runCatching { nextText().trim() }.getOrDefault("")

    private fun joinPath(base: String, href: String): String {
        val raw = href.substringBefore('#')
        val combined = if (base.isBlank()) raw else "$base/$raw"
        return normalizePath(combined)
    }

    private fun normalizePath(path: String): String {
        val segments = path.split("/")
        val result = mutableListOf<String>()
        for (segment in segments) {
            when (segment) {
                "", "." -> { /* skip empty segments and current-directory references */ }
                ".." -> if (result.isNotEmpty()) result.removeAt(result.size - 1)
                else -> result.add(segment)
            }
        }
        return result.joinToString("/")
    }

    private data class PackageInfo(
        val title: String,
        val author: String,
        val manifest: Map<String, ManifestItem>,
        val spine: List<String>,
        val coverHref: String?
    )

    private data class ManifestItem(
        val id: String,
        val href: String,
        val mediaType: String,
        val title: String,
        val properties: String
    )
}
