package com.example.data.room

import kotlinx.coroutines.flow.Flow

class KhushbooRepository(
    private val productDao: ProductDao,
    private val businessDao: BusinessDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val profileDao: CustomerProfileDao,
    private val addressDao: AddressDao,
    private val wishlistDao: WishlistDao,
    private val notificationDao: NotificationDao,
    private val orderStatusHistoryDao: OrderStatusHistoryDao,
    private val couponDao: CouponDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allBusinesses: Flow<List<BusinessEntity>> = businessDao.getAllBusinesses()
    val approvedBusinesses: Flow<List<BusinessEntity>> = businessDao.getApprovedBusinesses()
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getCartItems()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val customerProfile: Flow<CustomerProfileEntity?> = profileDao.getCustomerProfile()
    val addresses: Flow<List<AddressEntity>> = addressDao.getAllAddresses()
    val wishlist: Flow<List<WishlistItemEntity>> = wishlistDao.getWishlistItems()
    val notifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val allCoupons: Flow<List<CouponEntity>> = couponDao.getAllCoupons()

    fun getVendorProducts(vendorId: Long): Flow<List<ProductEntity>> = productDao.getProductsByVendor(vendorId)
    fun getVendorOrders(vendorId: Long): Flow<List<OrderEntity>> = orderDao.getOrdersByVendor(vendorId)

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)
    suspend fun insertProduct(product: ProductEntity) = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)

    suspend fun getBusinessById(id: Long): BusinessEntity? = businessDao.getBusinessById(id)
    suspend fun registerBusiness(business: BusinessEntity): Long = businessDao.insertBusiness(business)
    suspend fun updateBusiness(business: BusinessEntity) = businessDao.updateBusiness(business)
    suspend fun updateBusinessStatus(businessId: Long, status: String) = businessDao.updateBusinessStatus(businessId, status)

    suspend fun updateProfile(profile: CustomerProfileEntity) = profileDao.insertOrUpdateProfile(profile)
    suspend fun addAddress(address: AddressEntity) = addressDao.insertAddress(address)
    suspend fun deleteAddress(id: Long) = addressDao.deleteAddress(id)

    fun isWishlisted(productId: Long): Flow<Boolean> = wishlistDao.isInWishlist(productId)
    suspend fun toggleWishlist(productId: Long, isWishlisted: Boolean) {
        if (isWishlisted) {
            wishlistDao.removeFromWishlist(productId)
        } else {
            wishlistDao.addToWishlist(WishlistItemEntity(productId = productId))
        }
    }

    suspend fun addToCart(item: CartItemEntity) = cartDao.insertCartItem(item)
    suspend fun updateCartQuantity(id: Long, qty: Int) = cartDao.updateQuantity(id, qty)
    suspend fun removeFromCart(id: Long) = cartDao.deleteCartItem(id)
    suspend fun clearCart() = cartDao.clearCart()

    suspend fun createOrder(order: OrderEntity): Long {
        val orderId = orderDao.insertOrder(order)
        orderStatusHistoryDao.insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                previousStatus = "",
                newStatus = "ORDER_PLACED",
                changedBy = "Customer"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Order Placed #${orderId}",
                message = "Your order of ₹${order.totalAmount} has been placed successfully."
            )
        )
        return orderId
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String, changedBy: String) {
        val order = orderDao.getOrderById(orderId)
        val prev = order?.status ?: ""
        orderDao.updateOrderStatus(orderId, newStatus)
        orderStatusHistoryDao.insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                previousStatus = prev,
                newStatus = newStatus,
                changedBy = changedBy
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Order #${orderId} Updated",
                message = "Status changed to ${newStatus.replace("_", " ")} by $changedBy."
            )
        )
    }

    fun getOrderHistory(orderId: Long): Flow<List<OrderStatusHistoryEntity>> =
        orderStatusHistoryDao.getHistoryForOrder(orderId)

    suspend fun getCoupon(code: String): CouponEntity? = couponDao.getCouponByCode(code)
}
