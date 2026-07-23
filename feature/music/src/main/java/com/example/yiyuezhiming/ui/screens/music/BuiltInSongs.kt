package com.example.yiyuezhiming.ui.screens.music

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.yiyuezhiming.model.Song

/**
 * 内置歌曲注册表。
 *
 * 添加你录制的歌曲只需两步：
 * 1. 把音频文件（建议 mp3）放进 feature/music/src/main/res/raw/ 目录，
 *    文件名用小写字母和下划线，例如 first_song.mp3、our_story.mp3。
 * 2. 在下面的 list 里加一行 BuiltInSong(rawResName = "first_song", title = "歌名", artist = "演唱者")。
 *
 * rawResName 就是文件名去掉扩展名（first_song.mp3 → "first_song"）。
 * 内置歌曲会显示在播放列表最前面，和本地扫描到的歌曲一起播放。
 */
data class BuiltInSong(
    val rawResName: String,
    val title: String,
    val artist: String,
    val album: String = "以越之名"
)

object BuiltInSongs {

    // 在这里登记你录制的歌曲（目前为空，录好后按上面说明添加）：
    val list: List<BuiltInSong> = listOf(
        // BuiltInSong(rawResName = "first_song", title = "第一首歌", artist = "我"),
        // BuiltInSong(rawResName = "our_story", title = "我们的故事", artist = "我"),
    )

    /** 把注册的内置歌曲转换成 Song，用负数 ID 避免与 MediaStore 的正数 ID 冲突。 */
    fun toSongs(context: Context): List<Song> = list.mapIndexed { index, item ->
        val uri = Uri.parse("android.resource://${context.packageName}/raw/${item.rawResName}")
        Song(
            id = -(index + 1).toLong(),
            title = item.title,
            artist = item.artist,
            album = item.album,
            duration = extractDuration(context, uri),
            uri = uri.toString(),
            albumArtUri = null
        )
    }

    private fun extractDuration(context: Context, uri: Uri): Long = runCatching {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
        }
    }.getOrDefault(0L)
}
