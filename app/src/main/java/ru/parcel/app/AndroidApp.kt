package ru.parcel.app

import android.app.Application
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import ru.parcel.app.core.network.KtorInstance
import ru.parcel.app.core.service.DataFetchWorker
import ru.parcel.app.core.utils.DeviceUtils
import ru.parcel.app.di.prefs.ATMModel
import ru.parcel.app.di.prefs.SharedPrefModel
import ru.parcel.app.di.room.RoomModel
import ru.parcel.app.di.sync.DataSyncModel
import ru.parcel.app.di.theme.ThemeModel
import java.util.concurrent.TimeUnit

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AndroidApp)
            modules(
                SharedPrefModel.prefModule,
                RoomModel.appModule,
                ThemeModel.themeModule,
                ATMModel.atmModule,
                KtorInstance.ktorModule,
                DataSyncModel.dataSyncModule
            )
        }

        val workRequest = PeriodicWorkRequest.Builder(
            DataFetchWorker::class.java,
            3, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueue(workRequest)

        DeviceUtils.setDeviceId(baseContext)
    }
}