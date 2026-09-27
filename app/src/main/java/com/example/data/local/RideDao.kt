package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM rides ORDER BY timestamp DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity): Long

    @Query("SELECT * FROM rides WHERE rideId = :rideId LIMIT 1")
    suspend fun getRideById(rideId: String): RideEntity?

    @Query("DELETE FROM rides WHERE id = :id")
    suspend fun deleteRide(id: Long)

    @Query("DELETE FROM rides")
    suspend fun clearAllRides()
}
