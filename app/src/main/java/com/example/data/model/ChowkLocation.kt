package com.example.data.model

import kotlin.math.roundToInt
import kotlin.math.sqrt

data class ChowkLocation(
    val id: String,
    val name: String,
    val landmark: String,
    val x: Float, // Map grid coordinate X (0..1000)
    val y: Float, // Map grid coordinate Y (0..1000)
    val category: String,
    val popular: Boolean = false
) {
    fun distanceTo(other: ChowkLocation): Double {
        val dx = (x - other.x) * 0.012
        val dy = (y - other.y) * 0.012
        val dist = sqrt(dx * dx + dy * dy)
        return ((dist.coerceAtLeast(1.0) * 10.0).roundToInt()) / 10.0
    }

    companion object {
        val PRESET_CHOWKS = listOf(
            ChowkLocation(
                id = "c1",
                name = "Engro Chowk",
                landmark = "Engro Fertilizers Plant Gate & Colony, Daharki",
                x = 240f,
                y = 280f,
                category = "Industrial",
                popular = true
            ),
            ChowkLocation(
                id = "c2",
                name = "Main Bypass Chowk",
                landmark = "National Highway N-5, Daharki Bypass",
                x = 520f,
                y = 320f,
                category = "Highway",
                popular = true
            ),
            ChowkLocation(
                id = "c3",
                name = "Station Road Chowk",
                landmark = "Daharki Railway Station & Main Bazaar Road",
                x = 680f,
                y = 420f,
                category = "Transit",
                popular = true
            ),
            ChowkLocation(
                id = "c4",
                name = "Shahi Bazaar Chowk",
                landmark = "Central Shahi Bazaar & Sarafa Market, Daharki",
                x = 440f,
                y = 520f,
                category = "Commercial",
                popular = true
            ),
            ChowkLocation(
                id = "c5",
                name = "Mari Gas Gate Chowk",
                landmark = "Mari Petroleum (MPCL) Colony & Plant Road",
                x = 810f,
                y = 260f,
                category = "Industrial",
                popular = true
            ),
            ChowkLocation(
                id = "c6",
                name = "Civil Hospital Chowk",
                landmark = "Taluka Hospital & Police Station Thana, Daharki",
                x = 360f,
                y = 690f,
                category = "Health",
                popular = true
            ),
            ChowkLocation(
                id = "c7",
                name = "Reti Road Chowk",
                landmark = "Reti - Daharki Link Road & Truck Stand",
                x = 760f,
                y = 660f,
                category = "Transit",
                popular = false
            ),
            ChowkLocation(
                id = "c8",
                name = "Zarkoon Mill Chowk",
                landmark = "Zarkoon Textile Mills & Industrial Estate",
                x = 200f,
                y = 780f,
                category = "Industrial",
                popular = false
            ),
            ChowkLocation(
                id = "c9",
                name = "Galla Mandi Chowk",
                landmark = "Grain Market & Sabzi Mandi, Daharki",
                x = 580f,
                y = 750f,
                category = "Commercial",
                popular = false
            ),
            ChowkLocation(
                id = "c10",
                name = "Mirpur Mathelo Cutoff",
                landmark = "Highway N-5 Link to Mirpur Mathelo",
                x = 150f,
                y = 480f,
                category = "Highway",
                popular = false
            )
        )
    }
}
