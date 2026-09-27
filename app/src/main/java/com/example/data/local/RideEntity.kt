package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rideId: String,
    val pickupName: String,
    val dropName: String,
    val rideCategory: String,
    val rideTitle: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val fare: Double,
    val paymentMethod: String,
    val driverName: String,
    val vehiclePlate: String,
    val status: String,
    val ratingGiven: Int = 5,
    val tipAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
