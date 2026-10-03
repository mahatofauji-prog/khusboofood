package com.example

import com.example.data.room.ProductEntity
import com.example.ui.customer.getProductImageRes
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun `verify dynamic weight calculation matches exact specification`() {
    // Example: Gulab Jamun at ₹280/kg
    val gulabJamun = ProductEntity(
        name = "Gulab Jamun",
        category = "Indian Sweets",
        price = 280.0,
        unit = "kg"
    )

    assertEquals(70.0, gulabJamun.calculateWeightPrice(0.25), 0.001) // 250 g = ₹70
    assertEquals(140.0, gulabJamun.calculateWeightPrice(0.50), 0.001) // 500 g = ₹140
    assertEquals(280.0, gulabJamun.calculateWeightPrice(1.00), 0.001) // 1 kg = ₹280
    assertEquals(560.0, gulabJamun.calculateWeightPrice(2.00), 0.001) // 2 kg = ₹560
  }

  @Test
  fun `verify products with unprovided price are marked invalid price`() {
    val cake = ProductEntity(
        name = "Delicious Cake",
        category = "Cakes & Pastries",
        price = null,
        unit = "NOT PROVIDED"
    )
    assertNull(cake.price)
    assertFalse(cake.hasValidPrice)
    assertEquals(0.0, cake.calculateWeightPrice(1.0), 0.001)
  }

  @Test
  fun `verify exactly 18 products have 18 unique image resources`() {
    val catalogue18 = listOf(
        "Special Kulhad Lassi",
        "Fruit Chat",
        "Gulab Jamun",
        "Sohan Papdi",
        "Pista Barfi",
        "Kaju Katli",
        "Bundi Laddu",
        "Besan Laddu",
        "Milk Cake",
        "Doda Burfi",
        "Ilaichi Barfi",
        "Chum Chum",
        "Bengali Rasgulla",
        "Sponge Rasgulla",
        "Delicious Cake",
        "Pastry",
        "Pizza",
        "Burger"
    )

    assertEquals(18, catalogue18.size)

    val imageResList = catalogue18.map { name ->
      val res = getProductImageRes(name)
      assertTrue("Image resource for $name must be valid", res != 0)
      name to res
    }

    val uniqueResCount = imageResList.map { it.second }.toSet().size
    assertEquals("All 18 products must have unique, non-duplicated images", 18, uniqueResCount)
  }
}
