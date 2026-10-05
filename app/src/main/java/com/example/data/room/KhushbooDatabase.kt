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
        CouponEntity::class,
        CategoryEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class KhushbooDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
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
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: KhushbooDatabase) {
            val productDao = database.productDao()
            val categoryDao = database.categoryDao()
            val businessDao = database.businessDao()
            val couponDao = database.couponDao()
            val profileDao = database.customerProfileDao()
            val addressDao = database.addressDao()
            val notificationDao = database.notificationDao()
            val partnerDao = database.deliveryPartnerDao()
            val settingsDao = database.deliverySettingsDao()

            // 10 Data-driven Categories (Requirement 7)
            val categories = listOf(
                CategoryEntity(id = 1, categoryId = 1, name = "Indian Sweets", imageUrl = "hero_sweets_1791067917652", isActive = true, sortOrder = 1),
                CategoryEntity(id = 2, categoryId = 2, name = "Drinks", imageUrl = "img_prod_kulhad_lassi_1791068738451", isActive = true, sortOrder = 2),
                CategoryEntity(id = 3, categoryId = 3, name = "Chaat / Snacks", imageUrl = "hero_street_food_1791067932481", isActive = true, sortOrder = 3),
                CategoryEntity(id = 4, categoryId = 4, name = "Cakes & Pastries", imageUrl = "img_prod_delicious_cake_1791068914099", isActive = true, sortOrder = 4),
                CategoryEntity(id = 5, categoryId = 5, name = "Pizza", imageUrl = "img_prod_pizza_1791068937979", isActive = true, sortOrder = 5),
                CategoryEntity(id = 6, categoryId = 6, name = "Burgers", imageUrl = "img_prod_burger_1791068950542", isActive = true, sortOrder = 6),
                CategoryEntity(id = 7, categoryId = 7, name = "Bengali Special", imageUrl = "hero_bengali_1791067952360", isActive = true, sortOrder = 7),
                CategoryEntity(id = 8, categoryId = 8, name = "Desserts", imageUrl = "hero_desserts_1791067982877", isActive = true, sortOrder = 8),
                CategoryEntity(id = 9, categoryId = 9, name = "Fast Food", imageUrl = "img_prod_pizza_burger_1790683979853", isActive = true, sortOrder = 9),
                CategoryEntity(id = 10, categoryId = 10, name = "Traditional Food", imageUrl = "hero_signature_1791067902606", isActive = true, sortOrder = 10)
            )
            categoryDao.insertCategories(categories)

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

            // Products across stores - EXACT 18 PRODUCTS CATALOGUE
            val catalogueProductNames = listOf(
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

            // Remove any legacy products not in the exact 18 catalogue
            productDao.deleteProductsNotIn(catalogueProductNames)

            val initialProducts = listOf(
                // 1. DRINKS: Special Kulhad Lassi (Price: ₹40, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    categoryId = 2,
                    name = "Special Kulhad Lassi",
                    category = "Drinks",
                    price = 40.0,
                    unit = "kg",
                    price250g = 10.0,
                    price500g = 20.0,
                    price1kg = 40.0,
                    priceStandard = 40.0,
                    description = "Traditional Indian kulhad filled with thick creamy chilled lassi, garnished with thick malai, crushed pistachios, almonds, and saffron strands.",
                    imageUrl = "img_prod_kulhad_lassi_1791068738451",
                    rating = 4.9f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 2. CHAAT / SNACKS: Fruit Chat (Price: ₹40, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    categoryId = 3,
                    name = "Fruit Chat",
                    category = "Chaat / Snacks",
                    price = 40.0,
                    unit = "kg",
                    price250g = 10.0,
                    price500g = 20.0,
                    price1kg = 40.0,
                    priceStandard = 40.0,
                    description = "Colorful fresh fruit chaat in a premium serving bowl, cut pieces of juicy fruits tossed with tangy spices, lemon juice, and rock salt.",
                    imageUrl = "img_prod_fruit_chat_1791068751675",
                    rating = 4.7f,
                    isFeatured = false,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 3. INDIAN SWEETS: Gulab Jamun (Price: ₹280, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Gulab Jamun",
                    category = "Indian Sweets",
                    price = 280.0,
                    unit = "kg",
                    price250g = 70.0,
                    price500g = 140.0,
                    price1kg = 280.0,
                    priceStandard = 280.0,
                    description = "Glossy golden-brown gulab jamuns soaked in sugar syrup, infused with rose water and green cardamom, garnished with silver vark and pistachios.",
                    imageUrl = "img_prod_gulab_jamun_1791068764302",
                    rating = 4.9f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 4. INDIAN SWEETS: Sohan Papdi (Price: ₹280, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Sohan Papdi",
                    category = "Indian Sweets",
                    price = 280.0,
                    unit = "kg",
                    price250g = 70.0,
                    price500g = 140.0,
                    price1kg = 280.0,
                    priceStandard = 280.0,
                    description = "Flaky layered traditional sohan papdi pieces made from roasted gram flour and pure desi ghee, garnished with almonds and pistachios.",
                    imageUrl = "img_prod_sohan_papdi_1791068775391",
                    rating = 4.6f,
                    isFeatured = false,
                    isBestSeller = false,
                    isAvailable = true
                ),

                // 5. INDIAN SWEETS: Pista Barfi (Price: ₹450, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Pista Barfi",
                    category = "Indian Sweets",
                    price = 450.0,
                    unit = "kg",
                    price250g = 112.5,
                    price500g = 225.0,
                    price1kg = 450.0,
                    priceStandard = 450.0,
                    description = "Premium green pista barfi pieces with rich pistachio garnish, pure mawa fudge, and delicate silver foil.",
                    imageUrl = "img_prod_pista_barfi_1791068786980",
                    rating = 4.8f,
                    isFeatured = true,
                    isBestSeller = false,
                    isAvailable = true
                ),

                // 6. INDIAN SWEETS: Kaju Katli (Price: ₹900, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Kaju Katli",
                    category = "Indian Sweets",
                    price = 900.0,
                    unit = "kg",
                    price250g = 225.0,
                    price500g = 450.0,
                    price1kg = 900.0,
                    priceStandard = 900.0,
                    description = "Diamond-shaped kaju katli with pure silver leaf and authentic smooth cashew texture, made with top-grade cashews and pure desi ghee.",
                    imageUrl = "img_prod_kaju_katli_1791068799420",
                    rating = 5.0f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 7. INDIAN SWEETS: Bundi Laddu (Price: ₹180, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Bundi Laddu",
                    category = "Indian Sweets",
                    price = 180.0,
                    unit = "kg",
                    price250g = 45.0,
                    price500g = 90.0,
                    price1kg = 180.0,
                    priceStandard = 180.0,
                    description = "Round golden boondi laddus with visible boondi texture, infused with fragrant green cardamom and melon seeds in desi ghee.",
                    imageUrl = "img_prod_bundi_laddu_1791068813467",
                    rating = 4.7f,
                    isFeatured = false,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 8. INDIAN SWEETS: Besan Laddu (Price: ₹280, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Besan Laddu",
                    category = "Indian Sweets",
                    price = 280.0,
                    unit = "kg",
                    price250g = 70.0,
                    price500g = 140.0,
                    price1kg = 280.0,
                    priceStandard = 280.0,
                    description = "Traditional yellow/golden besan laddus with roasted gram flour texture, rich aroma of pure desi ghee, and crunchy dry fruits.",
                    imageUrl = "img_prod_besan_laddu_1791068826192",
                    rating = 4.8f,
                    isFeatured = false,
                    isBestSeller = false,
                    isAvailable = true
                ),

                // 9. INDIAN SWEETS: Milk Cake (Price: ₹380, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Milk Cake",
                    category = "Indian Sweets",
                    price = 380.0,
                    unit = "kg",
                    price250g = 95.0,
                    price500g = 190.0,
                    price1kg = 380.0,
                    priceStandard = 380.0,
                    description = "Rich Indian milk cake pieces with dense creamy texture, caramelized dark core and sweet velvety flavor.",
                    imageUrl = "img_prod_milk_cake_1791068837979",
                    rating = 4.9f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 10. INDIAN SWEETS: Doda Burfi (Price: ₹400, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Doda Burfi",
                    category = "Indian Sweets",
                    price = 400.0,
                    unit = "kg",
                    price250g = 100.0,
                    price500g = 200.0,
                    price1kg = 400.0,
                    priceStandard = 400.0,
                    description = "Traditional dark golden-brown doda burfi pieces with nuts, sprouted wheat and dense caramelized milk fudge.",
                    imageUrl = "img_prod_doda_burfi_1791068849177",
                    rating = 4.8f,
                    isFeatured = false,
                    isBestSeller = false,
                    isAvailable = true
                ),

                // 11. INDIAN SWEETS: Ilaichi Barfi (Price: ₹460, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Ilaichi Barfi",
                    category = "Indian Sweets",
                    price = 460.0,
                    unit = "kg",
                    price250g = 115.0,
                    price500g = 230.0,
                    price1kg = 460.0,
                    priceStandard = 460.0,
                    description = "Elegant white/cream cardamom barfi pieces with cardamom garnish, made from pure reduced milk solids and silver vark.",
                    imageUrl = "img_prod_ilaichi_barfi_1791068860606",
                    rating = 4.8f,
                    isFeatured = false,
                    isBestSeller = false,
                    isAvailable = true
                ),

                // 12. INDIAN SWEETS: Chum Chum (Price: ₹340, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Chum Chum",
                    category = "Indian Sweets",
                    price = 340.0,
                    unit = "kg",
                    price250g = 85.0,
                    price500g = 170.0,
                    price1kg = 340.0,
                    priceStandard = 340.0,
                    description = "Traditional Bengali chum chum sweets with soft elongated shape, delicate mawa stuffing, rolled in fine desiccated coconut.",
                    imageUrl = "img_prod_chum_chum_1791068873447",
                    rating = 4.9f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 13. INDIAN SWEETS: Bengali Rasgulla (Price: ₹320, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Bengali Rasgulla",
                    category = "Indian Sweets",
                    price = 320.0,
                    unit = "kg",
                    price250g = 80.0,
                    price500g = 160.0,
                    price1kg = 320.0,
                    priceStandard = 320.0,
                    description = "White Bengali rasgullas soaked in clear sugar syrup, made with fresh chhena cheese for an authentic melt-in-mouth experience.",
                    imageUrl = "img_prod_bengali_rasgulla_1791068887214",
                    rating = 4.9f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 14. INDIAN SWEETS: Sponge Rasgulla (Price: ₹400, Unit: kg)
                ProductEntity(
                    vendorId = 1,
                    name = "Sponge Rasgulla",
                    category = "Indian Sweets",
                    price = 400.0,
                    unit = "kg",
                    price250g = 100.0,
                    price500g = 200.0,
                    price1kg = 400.0,
                    priceStandard = 400.0,
                    description = "Soft spongy rasgullas with a visibly different fluffy texture, airy porous chhena soaked in lightly sweetened cardamom syrup.",
                    imageUrl = "img_prod_sponge_rasgulla_1791068901167",
                    rating = 4.9f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 15. CAKES & PASTRIES: Delicious Cake (Price: NOT PROVIDED, Unit: NOT PROVIDED)
                ProductEntity(
                    vendorId = 1,
                    categoryId = 4,
                    name = "Delicious Cake",
                    category = "Cakes & Pastries",
                    price = null,
                    unit = "NOT PROVIDED",
                    price250g = 0.0,
                    price500g = 0.0,
                    price1kg = 0.0,
                    priceStandard = 0.0,
                    description = "Premium decorated celebration cake with artisan frosting. Price not provided - contact store or await pricing update.",
                    imageUrl = "img_prod_delicious_cake_1791068914099",
                    rating = 4.8f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 16. CAKES & PASTRIES: Pastry (Price: NOT PROVIDED, Unit: NOT PROVIDED)
                ProductEntity(
                    vendorId = 1,
                    categoryId = 4,
                    name = "Pastry",
                    category = "Cakes & Pastries",
                    price = null,
                    unit = "NOT PROVIDED",
                    price250g = 0.0,
                    price500g = 0.0,
                    price1kg = 0.0,
                    priceStandard = 0.0,
                    description = "Premium single-serving pastry with delicate layers and rich glaze. Price not provided - contact store or await pricing update.",
                    imageUrl = "img_prod_pastry_1791068925551",
                    rating = 4.7f,
                    isFeatured = false,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 17. FAST FOOD: Pizza (Price: NOT PROVIDED, Unit: NOT PROVIDED)
                ProductEntity(
                    vendorId = 1,
                    categoryId = 5,
                    name = "Pizza",
                    category = "Pizza",
                    price = null,
                    unit = "NOT PROVIDED",
                    price250g = 0.0,
                    price500g = 0.0,
                    price1kg = 0.0,
                    priceStandard = 0.0,
                    description = "Premium freshly baked pizza with appetizing toppings, melted bubbly cheese and golden crust. Price not provided.",
                    imageUrl = "img_prod_pizza_1791068937979",
                    rating = 4.7f,
                    isFeatured = true,
                    isBestSeller = true,
                    isAvailable = true
                ),

                // 18. FAST FOOD: Burger (Price: NOT PROVIDED, Unit: NOT PROVIDED)
                ProductEntity(
                    vendorId = 1,
                    categoryId = 6,
                    name = "Burger",
                    category = "Burgers",
                    price = null,
                    unit = "NOT PROVIDED",
                    price250g = 0.0,
                    price500g = 0.0,
                    price1kg = 0.0,
                    priceStandard = 0.0,
                    description = "Premium juicy burger with toasted bun, seasoned patty, cheese slice and crisp veggies. Price not provided.",
                    imageUrl = "img_prod_burger_1791068950542",
                    rating = 4.7f,
                    isFeatured = false,
                    isBestSeller = true,
                    isAvailable = true
                )
            )

            for (p in initialProducts) {
                val existing = productDao.getProductByName(p.name)
                if (existing == null) {
                    productDao.insertProduct(p)
                } else {
                    productDao.updateProduct(
                        p.copy(
                            id = existing.id,
                            createdAt = existing.createdAt,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
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
