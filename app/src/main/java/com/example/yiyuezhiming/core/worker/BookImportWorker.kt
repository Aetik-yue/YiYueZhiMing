package com.example.yiyuezhiming.core.worker

import android.content.Context
import android.net.Uri
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
        val uri = inputData.getString(KEY_URI)?.let(Uri::parse) ?: return Result.failure()
        val name = inputData.getString(KEY_NAME)
        return runCatching {
            repository.importEpub(uri, name)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() }
        )
    }

    companion object {
        const val KEY_URI = "uri"
        const val KEY_NAME = "name"
    }
}
