package ru.gdlbo.parcelradar.app.di.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.core.network.api.entity.Profile
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.repository.ParcelsRepository
import ru.gdlbo.parcelradar.app.core.repository.ProfileRepository

class RoomManager(
    val profileRepository: ProfileRepository,
    val parcelsRepository: ParcelsRepository,
) {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            loadProfile()
            loadParcels()
        }
    }

    fun insertProfile(profile: Profile) {
        coroutineScope.launch {
            profileRepository.insertProfile(profile)
            loadProfile()
        }
    }

    fun updateNotifySettings(notifyEmail: Boolean, notifyPush: Boolean) {
        coroutineScope.launch {
            profileRepository.updateNotifySettings(notifyEmail, notifyPush)
            loadProfile()
        }
    }

    fun insertParcels(tracking: List<Tracking>) {
        coroutineScope.launch {
            parcelsRepository.insert(tracking)
            loadParcels()
        }
    }

    fun insertParcel(tracking: Tracking) {
        coroutineScope.launch {
            parcelsRepository.insert(tracking)
            loadParcels()
        }
    }

    suspend fun dropParcels() {
        parcelsRepository.deleteAll()
    }

    suspend fun dropDb() {
        parcelsRepository.deleteAll()
        profileRepository.deleteAll()
    }

    suspend fun loadNotifySettings(): Pair<Boolean, Boolean> {
        return profileRepository.getNotifySettings()
    }

    suspend fun getTrackingById(id: Long): Tracking? {
        return parcelsRepository.getTrackingById(id)
    }

    suspend fun removeTrackingById(parcel: Tracking) {
        return parcelsRepository.delete(parcel)
    }

    suspend fun loadProfile(): Profile? {
        return profileRepository.getProfile()
    }

    suspend fun loadParcels(): List<Tracking> {
        return parcelsRepository.getAllTrackings()
    }
}