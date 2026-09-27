package com.example.data.local

import kotlinx.coroutines.flow.Flow

class RideRepository(private val rideDao: RideDao) {
    val allRides: Flow<List<RideEntity>> = rideDao.getAllRides()

    suspend fun insertRide(ride: RideEntity): Long {
        return rideDao.insertRide(ride)
    }

    suspend fun getRideById(rideId: String): RideEntity? {
        return rideDao.getRideById(rideId)
    }

    suspend fun deleteRide(id: Long) {
        rideDao.deleteRide(id)
    }

    suspend fun clearHistory() {
        rideDao.clearAllRides()
    }
}
