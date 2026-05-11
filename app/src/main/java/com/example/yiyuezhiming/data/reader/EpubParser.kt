package com.example.yiyuezhiming.data.reader

import android.util.Xml
import java.io.File
import java.io.InputStream
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
    fun parse(input: InputStream): EpubMetadata {
        val entries = readZip(input)
        val container = entries["META-INF/container.xml"] ?: error("找不到 EPUB 入口文件")
        val opfPath = parseContainer(container)
        val opf = entries[opfPath] ?: error("找不到 EPUB 内容清单")
        val opfBase = opfPath.substringBeforeLast('/', "")
        val packageInfo = parseOpf(opf, opfBase)
        val chapters = packageInfo.spine.mapIndexedNotNull { index, idref ->
            val item = packageInfo.manifest[idref] ?: return@mapIndexedNotNull null
            val href = item.href
            val content = entries[href] ?: return@mapIndexedNotNull null
            EpubChapter(
                id = item.id.ifBlank { "chapter_$index" },
                href = href,
                title = item.title.ifBlank { "第 ${index + 1} 章" },
                content = cleanXhtml(content.decodeToString())
            )
        }
        if (chapters.isEmpty()) error("没有解析到可阅读章节")
        val cover = packageInfo.coverHref?.let { entries[it] }
        return EpubMetadata(
            title = packageInfo.title.ifBlank { File(opfPath).nameWithoutExtension.ifBlank { "未命名书籍" } },
            author = packageInfo.author.ifBlank { "未知作者" },
            coverImage = cover,
            chapters = chapters
        )
    }

    private fun readZip(input: InputStream): Map<String, ByteArray> {
        val map = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    map[entry.name] = zip.readBytes()
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
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun XmlPullParser.nextTextSafe(): String =
        runCatching { nextText().trim() }.getOrDefault("")

    private fun joinPath(base: String, href: String): String {
        val raw = href.substringBefore('#')
        if (base.isBlank()) return raw
        return "$base/$raw".replace(Regex("/+"), "/")
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
