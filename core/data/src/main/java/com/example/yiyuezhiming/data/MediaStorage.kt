package com.example.yiyuezhiming.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun copyToPrivateStorage(source: Uri, kind: Kind): String {
        val directory = File(context.filesDir, kind.folder).apply { mkdirs() }
        val extension = when (kind) {
            Kind.Photo -> ".jpg"
            Kind.Music -> ".mp3"
        }
        val target = File(directory, "${kind.prefix}_${System.currentTimeMillis()}$extension")
        context.contentResolver.openInputStream(source).use { input ->
            requireNotNull(input) { "无法读取选择的文件" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return Uri.fromFile(target).toString()
    }

    fun readPhotoDate(source: Uri): LocalDate {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        val millis = runCatching {
            context.contentResolver.query(source, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val taken = cursor.getLongOrNull(MediaStore.Images.Media.DATE_TAKEN)
                val added = cursor.getLongOrNull(MediaStore.Images.Media.DATE_ADDED)?.secondsToMillis()
                val modified = cursor.getLongOrNull(MediaStore.Images.Media.DATE_MODIFIED)?.secondsToMillis()
                listOfNotNull(taken, added, modified).firstOrNull { it > 0L }
            }
        }.getOrNull()

        return millis
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            ?: LocalDate.now()
    }

    private fun android.database.Cursor.getLongOrNull(columnName: String): Long? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private fun Long.secondsToMillis(): Long = if (this < 10_000_000_000L) this * 1000L else this

    enum class Kind(val folder: String, val prefix: String) {
        Photo("photos", "photo"),
        Music("music", "music")
    }
}
