package ru.parcel.app.core.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.parcel.app.core.dao.ParcelsDao
import ru.parcel.app.core.network.model.Tracking

class ParcelsRepository(private val parcelsDao: ParcelsDao) {
    suspend fun getAllTrackings(): List<Tracking> {
        return withContext(Dispatchers.IO) {
            parcelsDao.getAll()
        }
    }

    suspend fun insert(trackings: List<Tracking>) {
        withContext(Dispatchers.IO) {
            parcelsDao.insertAll(trackings)
        }
    }

    suspend fun insert(tracking: Tracking) {
        withContext(Dispatchers.IO) {
            parcelsDao.insert(tracking)
        }
    }

    suspend fun getTrackingById(id: Long): Tracking? {
        return withContext(Dispatchers.IO) {
            parcelsDao.getById(id)
        }
    }

    suspend fun delete(tracking: Tracking) {
        withContext(Dispatchers.IO) {
            parcelsDao.delete(tracking)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            parcelsDao.deleteAll()
        }
    }
}