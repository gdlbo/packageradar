package ru.parcel.app.di.room

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.parcel.app.core.network.model.Checkpoint
import ru.parcel.app.core.network.model.Courier
import ru.parcel.app.core.network.model.Extra

class Converters {
    @TypeConverter
    fun fromCheckpointList(value: List<Checkpoint>?): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toCheckpointList(value: String): List<Checkpoint>? {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromCourier(value: Courier?): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toCourier(value: String): Courier? {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromExtraList(value: List<Extra>?): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toExtraList(value: String): List<Extra>? {
        return Json.decodeFromString(value)
    }
}