package ru.parcel.app.core.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.parcel.app.di.sync.DataSyncManager

class DataFetchWorker(
    context: Context,
    params: WorkerParameters,
    private val dataSyncManager: DataSyncManager
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                dataSyncManager.syncData(applicationContext)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}