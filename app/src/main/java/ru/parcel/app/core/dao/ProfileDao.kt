package ru.parcel.app.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.parcel.app.core.network.api.entity.Profile

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile")
    fun getProfile(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Update
    suspend fun updateProfile(profile: Profile)

    @Query("DELETE FROM profile")
    suspend fun deleteAll()
}