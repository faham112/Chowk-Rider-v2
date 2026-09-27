package com.example.data.model

import kotlin.math.roundToInt

enum class RideCategory {
    CHINGCHI,
    MOTO,
    AUTO,
    CAR
}

data class RideType(
    val category: RideCategory,
    val title: String,
    val subtitle: String,
    val capacity: Int,
    val baseFare: Double,
    val perKmRate: Double,
    val averageSpeedKmh: Double,
    val tag: String? = null
) {
    fun calculateFare(distanceKm: Double, promoDiscountPercent: Int = 0): Double {
        val rawFare = baseFare + (distanceKm * perKmRate)
        val discounted = rawFare * (1.0 - (promoDiscountPercent.coerceIn(0, 80) / 100.0))
        return (discounted.roundToInt()).toDouble()
    }

    fun estimateDurationMinutes(distanceKm: Double): Int {
        val hours = distanceKm / averageSpeedKmh
        val minutes = (hours * 60).roundToInt() + 2 // traffic overhead
        return minutes.coerceAtLeast(3)
    }

    companion object {
        val ALL_TYPES = listOf(
            RideType(
                category = RideCategory.CHINGCHI,
                title = "Chowk Chingchi",
                subtitle = "6-seater Qingqi rickshaw, Daharki local favorite",
                capacity = 6,
                baseFare = 40.0,
                perKmRate = 12.0,
                averageSpeedKmh = 28.0,
                tag = "Most Popular"
            ),
            RideType(
                category = RideCategory.MOTO,
                title = "Chowk Bike",
                subtitle = "70cc / 125cc motorbike, fastest through bazaar",
                capacity = 1,
                baseFare = 30.0,
                perKmRate = 10.0,
                averageSpeedKmh = 38.0,
                tag = "Fastest"
            ),
            RideType(
                category = RideCategory.AUTO,
                title = "Chowk Auto",
                subtitle = "Private 3-wheeler auto rickshaw & luggage",
                capacity = 3,
                baseFare = 60.0,
                perKmRate = 18.0,
                averageSpeedKmh = 32.0,
                tag = "Comfort"
            ),
            RideType(
                category = RideCategory.CAR,
                title = "Chowk Car",
                subtitle = "Suzuki Mehran / Alto AC car for highway & family",
                capacity = 4,
                baseFare = 120.0,
                perKmRate = 35.0,
                averageSpeedKmh = 45.0,
                tag = "AC Ride"
            )
        )
    }
}
