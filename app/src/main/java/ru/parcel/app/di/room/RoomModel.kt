package ru.parcel.app.di.room

import android.content.Context
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.parcel.app.core.AppDatabase
import ru.parcel.app.core.repository.ParcelsRepository
import ru.parcel.app.core.repository.ProfileRepository

object RoomModel {
    val appModule = module {
        single { buildDatabase(androidApplication()) }
        single { get<AppDatabase>().profileDao() }
        single { get<AppDatabase>().parcelsDao() }
        single { ProfileRepository(get()) }
        single { ParcelsRepository(get()) }
        single { RoomManager(get(), get()) }
    }
}

fun buildDatabase(context: Context) =
    Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "app-database"
    ).build()