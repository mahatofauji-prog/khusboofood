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

    @Query("SELECT * FROM businesses WHERE status = 'APPROVED'")
    fun getApprovedBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity): Long

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("UPDATE businesses SET status = :status WHERE id = :businessId")
    suspend fun updateBusinessStatus(businessId: Long, status: String)
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
}

@Dao
interface CustomerProfileDao {
    @Query("SELECT * FROM customer_profiles WHERE id = 1")
    fun getCustomerProfile(): Flow<CustomerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CustomerProfileEntity)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: Long)
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
