package com.example.data.room

import kotlinx.coroutines.flow.Flow

class KhushbooRepository(
    private val productDao: ProductDao,
    private val businessDao: BusinessDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val profileDao: CustomerProfileDao,
    private val addressDao: AddressDao,
    private val partnerDao: DeliveryPartnerDao,
    private val assignmentDao: DeliveryAssignmentDao,
    private val settingsDao: DeliverySettingsDao,
    private val wishlistDao: WishlistDao,
    private val notificationDao: NotificationDao,
    private val orderStatusHistoryDao: OrderStatusHistoryDao,
    private val couponDao: CouponDao,
    private val categoryDao: CategoryDao
) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
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
    val allPartners: Flow<List<DeliveryPartnerEntity>> = partnerDao.getAllPartners()
    val availablePartners: Flow<List<DeliveryPartnerEntity>> = partnerDao.getAvailablePartners()
    val deliverySettings: Flow<DeliverySettingsEntity?> = settingsDao.getSettings()

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
    suspend fun updateStoreOpenStatus(businessId: Long, isOpen: Boolean) = businessDao.updateStoreOpenStatus(businessId, isOpen)

    suspend fun updateProfile(profile: CustomerProfileEntity) = profileDao.insertOrUpdateProfile(profile)
    suspend fun markOnboardingComplete() = profileDao.markOnboardingComplete()
    suspend fun updateSelectedLocation(
        lat: Double, lng: Double, addr: String, locality: String, city: String, state: String, pincode: String
    ) = profileDao.updateSelectedLocation(lat, lng, addr, locality, city, state, pincode)

    suspend fun addAddress(address: AddressEntity): Long = addressDao.insertAddress(address)
    suspend fun updateAddress(address: AddressEntity) = addressDao.updateAddress(address)
    suspend fun deleteAddress(id: Long) = addressDao.deleteAddress(id)
    suspend fun setDefaultAddress(id: Long) {
        addressDao.clearDefaultFlags()
        addressDao.setDefaultAddress(id)
    }
    suspend fun getAddressById(id: Long): AddressEntity? = addressDao.getAddressById(id)

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
                newStatus = order.status,
                changedBy = "Customer"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Order #${orderId} Placed",
                message = "Your order from ${order.storeName} of ₹${order.totalAmount} has been placed successfully."
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

    suspend fun assignDeliveryPartner(
        orderId: Long, partnerId: Long, partnerName: String, partnerPhone: String, mode: String
    ) {
        orderDao.assignDeliveryPartner(orderId, partnerId, partnerName, partnerPhone, "DELIVERY_ASSIGNED")
        assignmentDao.insertAssignment(
            DeliveryAssignmentEntity(
                orderId = orderId,
                partnerId = partnerId,
                deliveryMode = mode,
                status = "ASSIGNED"
            )
        )
        partnerDao.updateAvailability(partnerId, false)
        orderStatusHistoryDao.insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                previousStatus = "READY_FOR_PICKUP",
                newStatus = "DELIVERY_ASSIGNED",
                changedBy = "System Assignment"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Delivery Partner Assigned!",
                message = "$partnerName ($partnerPhone) has been assigned to deliver order #${orderId}."
            )
        )
    }

    fun getOrderHistory(orderId: Long): Flow<List<OrderStatusHistoryEntity>> =
        orderStatusHistoryDao.getHistoryForOrder(orderId)

    suspend fun getCoupon(code: String): CouponEntity? = couponDao.getCouponByCode(code)
}
