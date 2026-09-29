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
        WishlistItemEntity::class,
        NotificationEntity::class,
        OrderStatusHistoryEntity::class,
        CouponEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KhushbooDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun businessDao(): BusinessDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun customerProfileDao(): CustomerProfileDao
    abstract fun addressDao(): AddressDao
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

            // Main Outlet Business
            val mainBusiness = BusinessEntity(
                id = 1,
                name = "KHUSHBOO FOOD Main Outlet",
                ownerName = "Khushboo Food Management",
                ownerMobile = "6365839460",
                ownerEmail = "contact@khushboofood.com",
                category = "Sweets & Fast Food",
                description = "Authentic Indian Sweets, Pure Desi Ghee Delicacies & Fast Food",
                address = "Main Market Road, Near Temple, Sweets City",
                city = "Sweets City",
                state = "State",
                pincode = "110001",
                mapsUrl = "https://maps.app.goo.gl/owjmnmnfdecTksj5A?g_st=aw",
                openingTime = "08:00 AM",
                closingTime = "10:30 PM",
                gstNumber = "07AAAAA0000A1Z5",
                status = "APPROVED",
                rating = 4.9f
            )
            businessDao.insertBusiness(mainBusiness)

            // Initial Products
            val initialProducts = listOf(
                ProductEntity(
                    vendorId = 1,
                    name = "Special Kulhad Lassi",
                    category = "Beverages",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 80.0,
                    description = "Creamy, rich traditional Punjabi lassi served in an earthen kulhad topped with malai and dry fruits.",
                    imageUrl = "lassi",
                    rating = 4.9f, isFeatured = true, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Fruit Chat",
                    category = "Fast Food",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 120.0,
                    description = "Fresh seasonal fruits tossed with tangy Indian spices, lemon, and rock salt.",
                    imageUrl = "chat",
                    rating = 4.7f, isFeatured = false, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Gulab Jamun",
                    category = "Sweets",
                    price250g = 140.0, price500g = 270.0, price1kg = 520.0, priceStandard = 140.0,
                    description = "Soft, spongy milk-solid dumplings soaked in rose-scented sugar syrup.",
                    imageUrl = "sweets",
                    rating = 4.9f, isFeatured = true, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Sohan Papdi",
                    category = "Sweets",
                    price250g = 130.0, price500g = 250.0, price1kg = 480.0, priceStandard = 130.0,
                    description = "Flaky, crisp traditional Indian confection made with gram flour, ghee, and cardamom.",
                    imageUrl = "sweets",
                    rating = 4.6f, isFeatured = false, isBestSeller = false
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Pista Barfi",
                    category = "Barfi",
                    price250g = 200.0, price500g = 390.0, price1kg = 750.0, priceStandard = 200.0,
                    description = "Rich khoya barfi infused with crushed pistachios and edible silver foil.",
                    imageUrl = "kaju",
                    rating = 4.8f, isFeatured = true, isBestSeller = false
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Kaju Katli",
                    category = "Barfi",
                    price250g = 250.0, price500g = 480.0, price1kg = 950.0, priceStandard = 250.0,
                    description = "Legendary diamond-shaped cashew fudge made with premium cashews and pure ghee.",
                    imageUrl = "kaju",
                    rating = 5.0f, isFeatured = true, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Bundi Laddu",
                    category = "Laddu",
                    price250g = 120.0, price500g = 230.0, price1kg = 450.0, priceStandard = 120.0,
                    description = "Sweet golden gram flour pearls fried and bound together with ghee and dry fruits.",
                    imageUrl = "sweets",
                    rating = 4.7f, isFeatured = false, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Besan Laddu",
                    category = "Laddu",
                    price250g = 130.0, price500g = 250.0, price1kg = 480.0, priceStandard = 130.0,
                    description = "Classic roasted gram flour balls rich in ghee, cardamom, and nutty aroma.",
                    imageUrl = "sweets",
                    rating = 4.8f, isFeatured = false, isBestSeller = false
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Milk Cake",
                    category = "Sweets",
                    price250g = 180.0, price500g = 350.0, price1kg = 680.0, priceStandard = 180.0,
                    description = "Traditional caramelized milk fudge with a rich, grainy texture and deep flavor.",
                    imageUrl = "sweets",
                    rating = 4.9f, isFeatured = true, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Delicious Cake & Pastry",
                    category = "Cakes & Pastries",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 350.0,
                    description = "Freshly baked rich chocolate and fruit pastries made with premium ingredients.",
                    imageUrl = "cake",
                    rating = 4.7f, isFeatured = true, isBestSeller = true
                ),
                ProductEntity(
                    vendorId = 1,
                    name = "Pizza & Burger",
                    category = "Fast Food",
                    price250g = 0.0, price500g = 0.0, price1kg = 0.0, priceStandard = 199.0,
                    description = "Hot, cheesy wood-fired style pizza and crispy veg/paneer burgers.",
                    imageUrl = "pizza",
                    rating = 4.6f, isFeatured = true, isBestSeller = true
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
                    defaultAddress = "House 42, Main Market Road, Near Temple, Sweets City"
                )
            )
            addressDao.insertAddress(
                AddressEntity(
                    title = "Home",
                    addressLine = "House 42, Main Market Road, Near Temple",
                    city = "Sweets City",
                    pincode = "110001",
                    isDefault = true
                )
            )

            // Notifications
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Welcome to Khushboo Food Marketplace!",
                    message = "Explore fresh sweets, authentic delicacies & register your business easily."
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
