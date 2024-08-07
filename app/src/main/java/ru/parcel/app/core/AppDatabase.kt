package ru.parcel.app.core

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.parcel.app.core.dao.ParcelsDao
import ru.parcel.app.core.dao.ProfileDao
import ru.parcel.app.core.network.api.entity.Profile
import ru.parcel.app.core.network.model.Tracking
import ru.parcel.app.di.room.Converters

@Database(entities = [Profile::class, Tracking::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun parcelsDao(): ParcelsDao
}