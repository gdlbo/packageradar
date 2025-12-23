package ru.gdlbo.parcelradar.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import ru.gdlbo.parcelradar.app.core.network.ktorModule
import ru.gdlbo.parcelradar.app.core.utils.DeviceUtils
import ru.gdlbo.parcelradar.app.di.prefs.ATMModel
import ru.gdlbo.parcelradar.app.di.prefs.SharedPrefModel
import ru.gdlbo.parcelradar.app.di.room.RoomModel
import ru.gdlbo.parcelradar.app.di.sync.DataSyncModel
import ru.gdlbo.parcelradar.app.di.theme.ThemeModel

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
                ktorModule,
                DataSyncModel.dataSyncModule
            )
        }

        DeviceUtils.setDeviceId(baseContext)
    }
}