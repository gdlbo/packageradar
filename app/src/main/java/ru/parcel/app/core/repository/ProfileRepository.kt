package ru.parcel.app.core.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.parcel.app.core.dao.ProfileDao
import ru.parcel.app.core.network.api.entity.Profile

class ProfileRepository(private val profileDao: ProfileDao) {
    suspend fun insertProfile(profile: Profile) {
        withContext(Dispatchers.IO) {
            profileDao.insertProfile(profile)
        }
    }

    suspend fun updateNotifySettings(notifyEmail: Boolean, notifyPush: Boolean) {
        withContext(Dispatchers.IO) {
            val profile = profileDao.getProfile()
            if (profile != null) {
                val updatedProfile = profile.copy(
                    notifyEmail = notifyEmail,
                    notifyPush = notifyPush
                )
                profileDao.updateProfile(updatedProfile)
            }
        }
    }

    suspend fun getNotifySettings(): Pair<Boolean, Boolean> {
        return withContext(Dispatchers.IO) {
            val profile = profileDao.getProfile()
            if (profile != null) {
                Log.d(
                    "ProfileRepository",
                    "Notification settings: email=${profile.notifyEmail}, push=${profile.notifyPush}"
                )
                Pair(profile.notifyEmail, profile.notifyPush)
            } else {
                Log.d(
                    "ProfileRepository",
                    "Profile not found, using default notification settings: email=false, push=false"
                )
                Pair(false, false)
            }
        }
    }

    suspend fun getProfile(): Profile? {
        return withContext(Dispatchers.IO) {
            profileDao.getProfile()
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            profileDao.deleteAll()
        }
    }
}