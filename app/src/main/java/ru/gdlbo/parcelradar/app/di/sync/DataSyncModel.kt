package ru.gdlbo.parcelradar.app.di.sync

import org.koin.dsl.module

object DataSyncModel {
    val dataSyncModule = module {
        single {
            DataSyncManager()
        }
    }
}