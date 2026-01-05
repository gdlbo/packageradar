package ru.gdlbo.parcelradar.app.core.dao

import androidx.room.*
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

    @Update
    suspend fun update(tracking: Tracking)

    @Query("UPDATE tracking SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean)

    @Query("DELETE FROM tracking")
    suspend fun deleteAll()
}