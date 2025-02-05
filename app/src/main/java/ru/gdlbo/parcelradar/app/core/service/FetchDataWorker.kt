package ru.gdlbo.parcelradar.app.core.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.di.sync.DataSyncManager

class DataFetchWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val dataSyncManager: DataSyncManager by inject()

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