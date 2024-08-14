package ru.parcel.app.di.sync

import org.koin.dsl.module

object DataSyncModel {
    val dataSyncModule = module {
        single {
            DataSyncManager()
        }
    }
}