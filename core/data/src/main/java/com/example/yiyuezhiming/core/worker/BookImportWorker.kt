package com.example.yiyuezhiming.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.yiyuezhiming.data.BookRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BookImportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: BookRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL)
        val name = inputData.getString(KEY_NAME)
        if (!url.isNullOrBlank()) {
            repository.importRemoteNovel(url, name)
            return Result.success()
        }
        val localPath = inputData.getString(KEY_LOCAL_PATH) ?: return Result.failure()
        val mime = inputData.getString(KEY_MIME)
        return runCatching {
            repository.importBook(localPath, name, mime)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() }
        )
    }

    companion object {
        const val KEY_LOCAL_PATH = "local_path"
        const val KEY_NAME = "name"
        const val KEY_MIME = "mime"
        const val KEY_URL = "url"
    }
}
