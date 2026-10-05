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

  @Test
  fun `verify Indian mobile number validation`() {
    val validNumbers = listOf("9876543210", "8123456789", "7001234567", "6365839460")
    val invalidNumbers = listOf("1234567890", "5987654321", "98765", "98765432100", "98765abcde", "")

    val indianMobileRegex = Regex("^[6-9]\\d{9}$")
    for (num in validNumbers) {
      assertTrue("Expected $num to be valid", num.matches(indianMobileRegex))
    }
    for (num in invalidNumbers) {
      assertFalse("Expected $num to be invalid", num.matches(indianMobileRegex))
    }
  }

  @Test
  fun `verify CustomerProfileEntity and AddressEntity initialization`() {
    val profile = com.example.data.room.CustomerProfileEntity(
      id = 1,
      userId = 1,
      name = "Anita Roy",
      mobileNumber = "9876543210",
      mobileVerified = true,
      email = "anita@example.com"
    )
    assertTrue(profile.mobileVerified)
    assertEquals("9876543210", profile.mobileNumber)

    val address = com.example.data.room.AddressEntity(
      id = 10,
      userId = 1,
      label = "Home",
      fullAddress = "Main Market Road, Purulia",
      locality = "Purulia Town",
      city = "Purulia",
      state = "West Bengal",
      postalCode = "723101",
      isDefault = true
    )
    assertEquals("Home", address.label)
    assertTrue(address.isDefault)
    assertEquals("Purulia Town", address.locality)
  }

  @Test
  fun `verify all 10 categories have valid, realistic and distinct image resources`() {
    val categories = listOf(
      "Indian Sweets",
      "Drinks",
      "Chaat / Snacks",
      "Cakes & Pastries",
      "Pizza",
      "Burgers",
      "Bengali Special",
      "Desserts",
      "Fast Food",
      "Traditional Food"
    )

    val imageMap = categories.associateWith { cat ->
      val res = com.example.ui.customer.getCategoryImageRes(cat)
      assertTrue("Image resource for category $cat must be non-zero", res != 0)
      res
    }

    // Ensure distinct images are used for visually distinct categories
    val uniqueImages = imageMap.values.toSet()
    assertTrue("Categories should have diverse unique images (at least 8 distinct assets)", uniqueImages.size >= 8)
  }

  @Test
  fun `verify category-based product filtering matches specification`() {
    val sampleProducts = listOf(
      ProductEntity(id = 1, categoryId = 2, name = "Special Kulhad Lassi", category = "Drinks"),
      ProductEntity(id = 2, categoryId = 3, name = "Fruit Chat", category = "Chaat / Snacks"),
      ProductEntity(id = 3, categoryId = 1, name = "Gulab Jamun", category = "Indian Sweets"),
      ProductEntity(id = 4, categoryId = 1, name = "Sohan Papdi", category = "Indian Sweets"),
      ProductEntity(id = 5, categoryId = 1, name = "Pista Barfi", category = "Indian Sweets"),
      ProductEntity(id = 6, categoryId = 1, name = "Kaju Katli", category = "Indian Sweets"),
      ProductEntity(id = 7, categoryId = 1, name = "Bundi Laddu", category = "Indian Sweets"),
      ProductEntity(id = 8, categoryId = 1, name = "Besan Laddu", category = "Indian Sweets"),
      ProductEntity(id = 9, categoryId = 1, name = "Milk Cake", category = "Indian Sweets"),
      ProductEntity(id = 10, categoryId = 1, name = "Doda Burfi", category = "Indian Sweets"),
      ProductEntity(id = 11, categoryId = 1, name = "Ilaichi Barfi", category = "Indian Sweets"),
      ProductEntity(id = 12, categoryId = 1, name = "Chum Chum", category = "Indian Sweets"),
      ProductEntity(id = 13, categoryId = 1, name = "Bengali Rasgulla", category = "Indian Sweets"),
      ProductEntity(id = 14, categoryId = 1, name = "Sponge Rasgulla", category = "Indian Sweets"),
      ProductEntity(id = 15, categoryId = 4, name = "Delicious Cake", category = "Cakes & Pastries"),
      ProductEntity(id = 16, categoryId = 4, name = "Pastry", category = "Cakes & Pastries"),
      ProductEntity(id = 17, categoryId = 5, name = "Pizza", category = "Pizza"),
      ProductEntity(id = 18, categoryId = 6, name = "Burger", category = "Burgers")
    )

    // Indian Sweets (categoryId = 1): exactly 12 sweet products
    val sweets = com.example.ui.customer.filterProductsForCategory(sampleProducts, "Indian Sweets", 1L)
    assertEquals(12, sweets.size)
    assertTrue(sweets.all { it.categoryId == 1L })

    // Drinks (categoryId = 2): Special Kulhad Lassi
    val drinks = com.example.ui.customer.filterProductsForCategory(sampleProducts, "Drinks", 2L)
    assertEquals(1, drinks.size)
    assertEquals("Special Kulhad Lassi", drinks.first().name)

    // Chaat / Snacks (categoryId = 3): Fruit Chat
    val chaat = com.example.ui.customer.filterProductsForCategory(sampleProducts, "Chaat / Snacks", 3L)
    assertEquals(1, chaat.size)
    assertEquals("Fruit Chat", chaat.first().name)

    // Cakes & Pastries (categoryId = 4): Delicious Cake, Pastry
    val cakes = com.example.ui.customer.filterProductsForCategory(sampleProducts, "Cakes & Pastries", 4L)
    assertEquals(2, cakes.size)
    assertTrue(cakes.any { it.name == "Delicious Cake" })
    assertTrue(cakes.any { it.name == "Pastry" })

    // Pizza (categoryId = 5)
    val pizza = com.example.ui.customer.filterProductsForCategory(sampleProducts, "Pizza", 5L)
    assertEquals(1, pizza.size)
    assertEquals("Pizza", pizza.first().name)

    // Burgers (categoryId = 6)
    val burgers = com.example.ui.customer.filterProductsForCategory(sampleProducts, "Burgers", 6L)
    assertEquals(1, burgers.size)
    assertEquals("Burger", burgers.first().name)

    // All (categoryId = 0 / null)
    val all = com.example.ui.customer.filterProductsForCategory(sampleProducts, "All", 0L)
    assertEquals(18, all.size)
  }

  @Test
  fun `verify dynamic weight pricing formula and currency formatting for 280 per kg and 450 per kg`() {
    val weightOptions = listOf(
        "250g" to 0.25,
        "500g" to 0.5,
        "1kg" to 1.0,
        "2kg" to 2.0
    )

    // Product 1: Base Price ₹280/kg
    val basePrice280 = 280.0
    val prices280 = weightOptions.associate { (label, mult) ->
      label to com.example.ui.customer.formatCurrency(basePrice280 * mult)
    }
    assertEquals("₹70", prices280["250g"])
    assertEquals("₹140", prices280["500g"])
    assertEquals("₹280", prices280["1kg"])
    assertEquals("₹560", prices280["2kg"])

    // Product 2: Base Price ₹450/kg
    val basePrice450 = 450.0
    val prices450 = weightOptions.associate { (label, mult) ->
      label to com.example.ui.customer.formatCurrency(basePrice450 * mult)
    }
    assertEquals("₹112.50", prices450["250g"])
    assertEquals("₹225", prices450["500g"])
    assertEquals("₹450", prices450["1kg"])
    assertEquals("₹900", prices450["2kg"])
  }

  @Test
  fun `verify CartItemEntity preserves productId storeId categoryId and variant`() {
    val cartItem = com.example.data.room.CartItemEntity(
        id = 1,
        productId = 3,
        vendorId = 2,
        categoryId = 1,
        storeName = "Royal Sweets & Delicacies",
        productName = "Gulab Jamun",
        variant = "500g",
        price = 140.0,
        quantity = 2
    )

    assertEquals(3L, cartItem.productId)
    assertEquals(2L, cartItem.vendorId)
    assertEquals(2L, cartItem.storeId)
    assertEquals(1L, cartItem.categoryId)
    assertEquals("500g", cartItem.variant)
    assertEquals(140.0, cartItem.price, 0.001)
    assertEquals(2, cartItem.quantity)
    assertEquals(280.0, cartItem.price * cartItem.quantity, 0.001)
  }
}
