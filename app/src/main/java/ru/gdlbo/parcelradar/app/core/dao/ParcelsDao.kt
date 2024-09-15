package ru.gdlbo.parcelradar.app.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.gdlbo.parcelradar.app.core.network.model.Tracking

@Dao
interface ParcelsDao {
    @Query("SELECT * FROM tracking")
    fun getAll(): List<Tracking>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trackings: List<Tracking>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracking: Tracking)

    @Query("SELECT * FROM tracking WHERE id = :id")
    fun getById(id: Long): Tracking?

    @Delete
    suspend fun delete(tracking: Tracking)

    @Query("DELETE FROM tracking")
    suspend fun deleteAll()
}