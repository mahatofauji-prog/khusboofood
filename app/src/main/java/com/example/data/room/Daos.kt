package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE vendorId = :vendorId")
    fun getProductsByVendor(vendorId: Long): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE status = 'APPROVED' AND isActive = 1")
    fun getApprovedBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity): Long

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("UPDATE businesses SET status = :status WHERE id = :businessId")
    suspend fun updateBusinessStatus(businessId: Long, status: String)

    @Query("UPDATE businesses SET isOpen = :isOpen WHERE id = :businessId")
    suspend fun updateStoreOpenStatus(businessId: Long, isOpen: Boolean)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE vendorId = :vendorId ORDER BY orderDate DESC")
    fun getOrdersByVendor(vendorId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: String)

    @Query("UPDATE orders SET deliveryPartnerId = :partnerId, deliveryPartnerName = :partnerName, deliveryPartnerPhone = :partnerPhone, status = :status WHERE id = :orderId")
    suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long, partnerName: String, partnerPhone: String, status: String)
}

@Dao
interface CustomerProfileDao {
    @Query("SELECT * FROM customer_profiles WHERE id = 1")
    fun getCustomerProfile(): Flow<CustomerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CustomerProfileEntity)

    @Query("UPDATE customer_profiles SET isVerified = 1, isOnboarded = 1 WHERE id = 1")
    suspend fun markOnboardingComplete()

    @Query("UPDATE customer_profiles SET selectedLatitude = :lat, selectedLongitude = :lng, selectedAddress = :addr, selectedLocality = :locality, selectedCity = :city, selectedState = :state, selectedPostalCode = :pincode WHERE id = 1")
    suspend fun updateSelectedLocation(lat: Double, lng: Double, addr: String, locality: String, city: String, state: String, pincode: String)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses ORDER BY isDefault DESC, id DESC")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getAddressById(id: Long): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: Long)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAddress(id: Long)
}

@Dao
interface DeliveryPartnerDao {
    @Query("SELECT * FROM delivery_partners")
    fun getAllPartners(): Flow<List<DeliveryPartnerEntity>>

    @Query("SELECT * FROM delivery_partners WHERE isOnline = 1 AND isAvailable = 1")
    fun getAvailablePartners(): Flow<List<DeliveryPartnerEntity>>

    @Query("SELECT * FROM delivery_partners WHERE id = :id")
    suspend fun getPartnerById(id: Long): DeliveryPartnerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: DeliveryPartnerEntity): Long

    @Update
    suspend fun updatePartner(partner: DeliveryPartnerEntity)

    @Query("UPDATE delivery_partners SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: Long, isAvailable: Boolean)
}

@Dao
interface DeliveryAssignmentDao {
    @Query("SELECT * FROM delivery_assignments WHERE orderId = :orderId")
    suspend fun getAssignmentForOrder(orderId: Long): DeliveryAssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: DeliveryAssignmentEntity): Long

    @Query("UPDATE delivery_assignments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface DeliverySettingsDao {
    @Query("SELECT * FROM delivery_settings WHERE id = 1")
    fun getSettings(): Flow<DeliverySettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: DeliverySettingsEntity)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist")
    fun getWishlistItems(): Flow<List<WishlistItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE productId = :productId)")
    fun isInWishlist(productId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist WHERE productId = :productId")
    suspend fun removeFromWishlist(productId: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
}

@Dao
interface OrderStatusHistoryDao {
    @Query("SELECT * FROM order_status_history WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getHistoryForOrder(orderId: Long): Flow<List<OrderStatusHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: OrderStatusHistoryEntity)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE active = 1")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND active = 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)
}
