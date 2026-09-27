package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChowkLocation
import com.example.data.model.RideCategory
import com.example.data.model.RideType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Chowk Rider", appName)
  }

  @Test
  fun `verify daharki chowk locations and distance calculation`() {
    val chowks = ChowkLocation.PRESET_CHOWKS
    assertTrue(chowks.size >= 8)

    val engroChowk = chowks.first { it.name == "Engro Chowk" }
    val bypassChowk = chowks.first { it.name == "Main Bypass Chowk" }
    val distance = engroChowk.distanceTo(bypassChowk)
    assertTrue("Distance should be positive and realistic", distance >= 1.0)
  }

  @Test
  fun `verify daharki chingchi ride fare calculation and promo code discount`() {
    val chingchi = RideType.ALL_TYPES.first { it.category == RideCategory.CHINGCHI }
    assertEquals("Chowk Chingchi", chingchi.title)
    assertEquals(6, chingchi.capacity)

    val regularFare = chingchi.calculateFare(4.0, 0)
    val discountedFare = chingchi.calculateFare(4.0, 50)

    assertTrue("Regular fare should be greater than base fare", regularFare > chingchi.baseFare)
    assertEquals("50% promo should cut fare in half", (regularFare * 0.5).toInt(), discountedFare.toInt())
  }
}
