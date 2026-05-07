package com.example.yiyuezhiming.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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

    enum class Kind(val folder: String, val prefix: String) {
        Photo("photos", "photo"),
        Music("music", "music")
    }
}
