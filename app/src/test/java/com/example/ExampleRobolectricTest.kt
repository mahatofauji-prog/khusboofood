package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.LocationHelper
import org.junit.Assert.*
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
    assertEquals("Khushboo Food", appName)
  }

  @Test
  fun `haversine distance calculates accurate distance between coordinates`() {
    // Purulia Main (23.3322, 86.3652) to Purulia Station (23.3385, 86.3712)
    val distance = LocationHelper.calculateDistanceKm(23.3322, 86.3652, 23.3385, 86.3712)
    assertTrue("Distance should be around 0.9 - 1.2 km", distance in 0.5..2.0)
  }

  @Test
  fun `delivery fee is free when order exceeds threshold`() {
    val fee = LocationHelper.calculateDeliveryFee(distanceKm = 3.5, orderTotal = 550.0, freeThreshold = 499.0)
    assertEquals(0.0, fee, 0.01)
  }

  @Test
  fun `estimated delivery minutes calculation is realistic`() {
    val mins = LocationHelper.estimateDeliveryMinutes(3.0)
    assertTrue("ETA should be between 20 and 45 minutes", mins in 20..45)
  }

  @Test
  fun `splash background and branding colors conform to black and gold theme`() {
    val bg = com.example.ui.splash.SPLASH_BACKGROUND
    val gold = com.example.ui.splash.SPLASH_GOLD
    assertEquals(8f / 255f, bg.red, 0.01f)
    assertEquals(8f / 255f, bg.green, 0.01f)
    assertEquals(8f / 255f, bg.blue, 0.01f)
    assertEquals(0xF5.toFloat() / 255f, gold.red, 0.01f)
    assertEquals(0xC5.toFloat() / 255f, gold.green, 0.01f)
    assertEquals(0x42.toFloat() / 255f, gold.blue, 0.01f)
  }
}
