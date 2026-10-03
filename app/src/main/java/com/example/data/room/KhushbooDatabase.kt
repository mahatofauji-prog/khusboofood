package com.example.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        BusinessEntity::class,
        CustomerProfileEntity::class,
        AddressEntity::class,
        DeliveryPartnerEntity::class,
        DeliveryAssignmentEntity::class,
        DeliverySettingsEntity::class,
        WishlistItemEntity::class,
        NotificationEntity::class,
        OrderStatusHistoryEntity::class,
        CouponEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KhushbooDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun businessDao(): BusinessDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun customerProfileDao(): CustomerProfileDao
    abstract fun addressDao(): AddressDao
    abstract fun deliveryPartnerDao(): DeliveryPartnerDao
    abstract fun deliveryAssignmentDao(): DeliveryAssignmentDao
    abstract fun deliverySettingsDao(): DeliverySettingsDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun notificationDao(): NotificationDao
    abstract fun orderStatusHistoryDao(): OrderStatusHistoryDao
    abstract fun couponDao(): CouponDao

    companion object {
        @Volatile
        private var INSTANCE: KhushbooDatabase? = null

        fun getDatabase(context: Context): KhushbooDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhushbooDatabase::class.java,
                    "khushboo_food_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (database.productDao().getProductCount() == 0) {
                            populateInitialData(database)
                        }
                    }
                }
            }
        }

        suspend fun populateInitialData(database: KhushbooDatabase) {
            val productDao = database.productDao()
            val businessDao = database.businessDao()
            val couponDao = database.couponDao()
            val profileDao = database.customerProfileDao()
            val addressDao = database.addressDao()
            val notificationDao = database.notificationDao()
            val partnerDao = database.deliveryPartnerDao()
            val settingsDao = database.deliverySettingsDao()

            // Delivery Settings
            settingsDao.insertOrUpdateSettings(
                DeliverySettingsEntity(
                    id = 1,
                    baseDeliveryFee = 25.0,
                    freeDeliveryThreshold = 499.0,
                    perKmFee = 6.0,
                    baseDistanceKm = 2.0,
                    surgeFee = 0.0,
                    khusbooDeliveryEnabled = true,
                    thirdPartyDeliveryEnabled = true,
                    sellerSelfDeliveryEnabled = true,
                    customerPickupEnabled = true
                )
            )

            // Delivery Partners
            partnerDao.insertPartner(
                DeliveryPartnerEntity(
                    id = 1,
                    name = "Amit Kumar",
                    phone = "9876543210",
                    vehicleType = "Motorcycle",
                    vehicleNumber = "WB-56-1234",
                    currentLatitude = 23.3322,
                    currentLongitude = 86.3652,
                    isOnline = true,
                    isAvailable = true,
                    verificationStatus = "VERIFIED",
                    rating = 4.9f,
                    totalDeliveries = 142
                )
            )

            partnerDao.insertPartner(
                DeliveryPartnerEntity(
                    id = 2,
                    name = "Rakesh Singh",
                    phone = "9876543211",
                    vehicleType = "Electric Scooter",
                    vehicleNumber = "WB-56-5678",
                    currentLatitude = 23.3350,
                    currentLongitude = 86.3680,
                    isOnline = true,
                    isAvailable = true,
                    verificationStatus = "VERIFIED",
                    rating = 4.8f,
                    totalDeliveries = 89
                )
            )

            // Registered Multi-Location Stores
            val stores = listOf(
                BusinessEntity(
                    id = 1,
                    sellerId = 1,
                    name = "KHUSHBOO FOOD Main Outlet",
                    ownerName = "Khushboo Food Management",
                    ownerMobile = "6365839460",
                    ownerEmail = "contact@khushboofood.com",
                    category = "Sweets & Fast Food",
                    description = "Authentic Indian Sweets, Pure Desi Ghee Delicacies & Fast Food",
                    address = "Main Market Road, Near Temple, Purulia Town",
                    locality = "Purulia Town",
                    city = "Purulia",
                    state = "West Bengal",
                    pincode = "723101",
                    latitude = 23.3322,
                    longitude = 86.3652,
                    deliveryRadius = 12.0,
                    deliveryMode = "KHUSBOO_DELIVERY",
                    isOpen = true,
                    isActive = true,
                    mapsUrl = "https://maps.app.goo.gl/owjmnmnfdecTksj5A?g_st=aw",
                    openingTime = "08:00 AM",
                    closingTime = "10:30 PM",
                    gstNumber = "07AAAAA0000A1Z5",
                    status = "APPROVED",
                    rating = 4.9f
                ),
                BusinessEntity(
                    id = 2,
                    sellerId = 2,
                    name = "Royal Sweets & Delicacies",
                    ownerName = "Sunil Verma",
                    ownerMobile = "9835012345",
                    ownerEmail = "sunil.verma@royalsweets.com",
                    category = "Sweets & Confectionery",
                    description = "Famous Kaju Sweets, Dry Fruit Halwa & Special Bengali Sweets",
                    address = "City Center, Sector 4, Bokaro Steel City",
                    locality = "Sector 4",
                    city = "Bokaro",
                    state = "Jharkhand",
                    pincode = "827004",
                    latitude = 23.6693,
                    longitude = 86.1511,
                    deliveryRadius = 8.0,
                    deliveryMode = "SELLER_SELF_DELIVERY",
                    isOpen = true,
                    isActive = true,
                    mapsUrl = "",
                    openingTime = "09:00 AM",
                    closingTime = "10:00 PM",
                    gstNumber = "20BBBBB0000B1Z6",
                    status = "APPROVED",
                    rating = 4.8f
                ),
                BusinessEntity(
                    id = 3,
                    sellerId = 3,
                    name = "Sweet Bengal Sweets & Bakery",
                    ownerName = "Anirban Mukherjee",
                    ownerMobile = "9830098765",
                    ownerEmail = "anirban@sweetbengal.com",
                    category = "Bengali Sweets & Cakes",
                    description = "Authentic Nolen Gur Sandesh, Spongy Rasgulla & Fresh Fruit Pastries",
                    address = "Park Street, Central Kolkata",
                    locality = "Park Street",
                    city = "Kolkata",
                    state = "West Bengal",
                    pincode = "700016",
                    latitude = 22.5510,
                    longitude = 88.3524,
                    deliveryRadius = 10.0,
                    deliveryMode = "THIRD_PARTY_DELIVERY",
                    isOpen = true,
                    isActive = true,
                    mapsUrl = "",
                    openingTime = "08:30 AM",
                    closingTime = "10:30 PM",
                    gstNumber = "19CCCCC0000C1Z7",
                    status = "APPROVED",
                    rating = 4.9f
                ),
                BusinessEntity(
                    id = 4,
                    sellerId = 4,
                    name = "Desi Ghee Bakes & Desserts",
                    ownerName = "Rajesh Gupta",
                    ownerMobile = "9431054321",
                    ownerEmail = "rajesh.gupta@desighee.com",
                    category = "Sweets & Fast Food",
                    description = "Pure Desi Ghee Besan Laddu, Motichoor & Hot Snack Treats",
                    address = "Bank More, Main Market, Dhanbad",
                    locality = "Bank More",
                    city = "Dhanbad",
                    state = "Jharkhand",
                    pincode = "826001",
                    latitude = 23.7957,
                    longitude = 86.4304,
                    deliveryRadius = 7.0,
                    deliveryMode = "CUSTOMER_PICKUP",
                    isOpen = true,
                    isActive = true,
                    mapsUrl = "",
                    openingTime = "09:30 AM",
                    closingTime = "09:30 PM",
                    gstNumber = "20DDDDD0000D1Z8",
                    status = "APPROVED",
                    rating = 4.7f
                )
            )

            for (b in stores) {
                businessDao.insertBusiness(b)
            }

            // Products across stores
            val initialProducts = listOf(
                // Store 1: KHUSHBOO FOOD Main Outlet (Purulia)
                ProductEntity(
                    vendorId = 1,
                    name = "Special Kulhad Lassi",
                    category = "Beverages",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 80.0,
                    description = "Creamy, rich traditional Punjabi lassi served in an earthen kulhad topped with malai and dry fruits.",
                    imageUrl = "lassi",
                    rating = 4.9f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Fruit Chat",
                    category = "Fast Food",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 120.0,
                    description = "Fresh seasonal fruits tossed with tangy Indian spices, lemon, and rock salt.",
                    imageUrl = "chat",
                    rating = 4.7f, isFeatured = false, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Gulab Jamun",
                    category = "Sweets",
                    price250g = 140.0, price500g = 270.0, price1kg = 520.0, priceStandard = 140.0,
                    description = "Soft, spongy milk-solid dumplings soaked in rose-scented sugar syrup.",
                    imageUrl = "sweets",
                    rating = 4.9f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Sohan Papdi",
                    category = "Sweets",
                    price250g = 130.0, price500g = 250.0, price1kg = 480.0, priceStandard = 130.0,
                    description = "Flaky, crisp traditional Indian confection made with gram flour, ghee, and cardamom.",
                    imageUrl = "sweets",
                    rating = 4.6f, isFeatured = false, isBestSeller = false, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Pista Barfi",
                    category = "Barfi",
                    price250g = 200.0, price500g = 390.0, price1kg = 750.0, priceStandard = 200.0,
                    description = "Rich khoya barfi infused with crushed pistachios and edible silver foil.",
                    imageUrl = "kaju",
                    rating = 4.8f, isFeatured = true, isBestSeller = false, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Kaju Katli",
                    category = "Barfi",
                    price250g = 250.0, price500g = 480.0, price1kg = 950.0, priceStandard = 250.0,
                    description = "Legendary diamond-shaped cashew fudge made with premium cashews and pure ghee.",
                    imageUrl = "kaju",
                    rating = 5.0f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Bundi Laddu",
                    category = "Laddu",
                    price250g = 120.0, price500g = 230.0, price1kg = 450.0, priceStandard = 120.0,
                    description = "Sweet golden gram flour pearls fried and bound together with ghee and dry fruits.",
                    imageUrl = "sweets",
                    rating = 4.7f, isFeatured = false, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Besan Laddu",
                    category = "Laddu",
                    price250g = 130.0, price500g = 250.0, price1kg = 480.0, priceStandard = 130.0,
                    description = "Classic roasted gram flour balls rich in ghee, cardamom, and nutty aroma.",
                    imageUrl = "sweets",
                    rating = 4.8f, isFeatured = false, isBestSeller = false, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Milk Cake",
                    category = "Sweets",
                    price250g = 180.0, price500g = 350.0, price1kg = 680.0, priceStandard = 180.0,
                    description = "Traditional caramelized milk fudge with a rich, grainy texture and deep flavor.",
                    imageUrl = "sweets",
                    rating = 4.9f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Delicious Cake & Pastry",
                    category = "Cakes & Pastries",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 350.0,
                    description = "Freshly baked rich chocolate and fruit pastries made with premium ingredients.",
                    imageUrl = "cake",
                    rating = 4.7f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Pizza & Burger",
                    category = "Fast Food",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 199.0,
                    description = "Hot, cheesy wood-fired style pizza and crispy veg/paneer burgers.",
                    imageUrl = "pizza",
                    rating = 4.6f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                // Store 2: Royal Sweets (Bokaro)
                ProductEntity(
                    vendorId = 2,
                    name = "Royal Dry Fruit Halwa",
                    category = "Sweets",
                    price250g = 220.0, price500g = 420.0, price1kg = 800.0, priceStandard = 220.0,
                    description = "Chewy, rich Karachi halwa loaded with almonds, cashews, and pistachios.",
                    imageUrl = "sweets",
                    rating = 4.8f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                // Store 3: Sweet Bengal (Kolkata)
                ProductEntity(
                    vendorId = 3,
                    name = "Spongy Bengali Rasgulla",
                    category = "Bengali Sweets",
                    price250g = 150.0, price500g = 290.0, price1kg = 560.0, priceStandard = 150.0,
                    description = "Authentic melt-in-mouth Kolkata rasgullas crafted from fresh cow milk chhena.",
                    imageUrl = "sweets",
                    rating = 4.9f, isFeatured = true, isBestSeller = true, isAvailable = true
                ),
                // Store 4: Desi Ghee Bakes (Dhanbad)
                ProductEntity(
                    vendorId = 4,
                    name = "Pure Desi Ghee Motichoor",
                    category = "Laddu",
                    price250g = 160.0, price500g = 300.0, price1kg = 580.0, priceStandard = 160.0,
                    description = "Fine tiny pearls drenched in pure desi ghee and saffron syrup.",
                    imageUrl = "sweets",
                    rating = 4.7f, isFeatured = true, isBestSeller = false, isAvailable = true
                )
            )

            for (p in initialProducts) {
                productDao.insertProduct(p)
            }

            // Coupons
            couponDao.insertCoupon(
                CouponEntity(
                    code = "KHUSHBOO20",
                    discountPercent = 20,
                    maxDiscount = 150.0,
                    minOrderValue = 299.0
                )
            )
            couponDao.insertCoupon(
                CouponEntity(
                    code = "SWEET50",
                    discountPercent = 50,
                    maxDiscount = 100.0,
                    minOrderValue = 199.0
                )
            )

            // Initial Profile & Address
            profileDao.insertOrUpdateProfile(
                CustomerProfileEntity(
                    id = 1,
                    name = "Rahul Sharma",
                    mobile = "6365839460",
                    email = "rahul.sharma@gmail.com",
                    isVerified = true,
                    isOnboarded = true,
                    selectedLatitude = 23.3322,
                    selectedLongitude = 86.3652,
                    selectedAddress = "Main Market Road, Purulia Town, West Bengal 723101",
                    selectedLocality = "Purulia Town",
                    selectedCity = "Purulia",
                    selectedState = "West Bengal",
                    selectedPostalCode = "723101",
                    defaultAddress = "House 42, Main Market Road, Purulia Town"
                )
            )

            addressDao.insertAddress(
                AddressEntity(
                    userId = 1,
                    title = "Home",
                    fullAddress = "House 42, Main Market Road, Near Temple, Purulia Town, West Bengal 723101",
                    houseNumber = "House 42",
                    street = "Main Market Road",
                    locality = "Purulia Town",
                    city = "Purulia",
                    state = "West Bengal",
                    pincode = "723101",
                    latitude = 23.3322,
                    longitude = 86.3652,
                    contactName = "Rahul Sharma",
                    contactPhone = "6365839460",
                    isDefault = true
                )
            )

            addressDao.insertAddress(
                AddressEntity(
                    userId = 1,
                    title = "Work",
                    fullAddress = "Station Road Commercial Complex, Purulia, West Bengal 723102",
                    houseNumber = "Office 104",
                    street = "Station Road",
                    locality = "Purulia Station Area",
                    city = "Purulia",
                    state = "West Bengal",
                    pincode = "723102",
                    latitude = 23.3385,
                    longitude = 86.3712,
                    contactName = "Rahul Sharma",
                    contactPhone = "6365839460",
                    isDefault = false
                )
            )

            // Notifications
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Welcome to Khushboo Food Marketplace!",
                    message = "Discover fresh sweets & delicious food from verified stores near you."
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Special Offer 20% OFF",
                    message = "Use coupon KHUSHBOO20 to get 20% off on your first order above ₹299!"
                )
            )
        }
    }
}
