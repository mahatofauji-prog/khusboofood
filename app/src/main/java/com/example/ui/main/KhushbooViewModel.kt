package com.example.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.room.AddressEntity
import com.example.data.room.BusinessEntity
import com.example.data.room.CartItemEntity
import com.example.data.room.CustomerProfileEntity
import com.example.data.room.KhushbooDatabase
import com.example.data.room.KhushbooRepository
import com.example.data.room.NotificationEntity
import com.example.data.room.OrderEntity
import com.example.data.room.ProductEntity
import com.example.data.room.WishlistItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    CUSTOMER, VENDOR, DELIVERY, ADMIN
}

enum class CustomerScreen {
    HOME, LISTING, PRODUCT_DETAIL, CART, CHECKOUT, ORDER_TRACKING, MY_ORDERS, PROFILE, SEARCH, MORE_MENU, CREATE_BUSINESS_ACCOUNT, WISHLIST, ADDRESSES, COUPONS, NOTIFICATIONS, HELP_SUPPORT, ABOUT, REVIEWS
}

class KhushbooViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KhushbooRepository

    init {
        val db = KhushbooDatabase.getDatabase(application)
        repository = KhushbooRepository(
            db.productDao(),
            db.businessDao(),
            db.cartDao(),
            db.orderDao(),
            db.customerProfileDao(),
            db.addressDao(),
            db.wishlistDao(),
            db.notificationDao(),
            db.orderStatusHistoryDao(),
            db.couponDao()
        )
        viewModelScope.launch {
            if (db.productDao().getProductCount() == 0) {
                KhushbooDatabase.populateInitialData(db)
            }
        }
    }

    // Role state
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    // Customer Navigation & Filter State
    private val _customerScreen = MutableStateFlow(CustomerScreen.HOME)
    val customerScreen: StateFlow<CustomerScreen> = _customerScreen.asStateFlow()

    fun navigateCustomerTo(screen: CustomerScreen) {
        _customerScreen.value = screen
    }

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _customerScreen.value = CustomerScreen.LISTING
    }

    private val _selectedProductId = MutableStateFlow<Long?>(null)
    val selectedProductId: StateFlow<Long?> = _selectedProductId.asStateFlow()

    fun selectProduct(id: Long) {
        _selectedProductId.value = id
        _customerScreen.value = CustomerScreen.PRODUCT_DETAIL
    }

    private val _selectedOrderIdForTracking = MutableStateFlow<Long?>(null)
    val selectedOrderIdForTracking: StateFlow<Long?> = _selectedOrderIdForTracking.asStateFlow()

    fun viewOrderTracking(orderId: Long) {
        _selectedOrderIdForTracking.value = orderId
        _customerScreen.value = CustomerScreen.ORDER_TRACKING
    }

    // Active Vendor ID for Business Owner Data Isolation
    private val _selectedVendorId = MutableStateFlow<Long>(1L)
    val selectedVendorId: StateFlow<Long> = _selectedVendorId.asStateFlow()

    fun selectVendorId(vendorId: Long) {
        _selectedVendorId.value = vendorId
    }

    // Data Flows
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businesses: StateFlow<List<BusinessEntity>> = repository.allBusinesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedBusinesses: StateFlow<List<BusinessEntity>> = repository.approvedBusinesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<CustomerProfileEntity?> = repository.customerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val addresses: StateFlow<List<AddressEntity>> = repository.addresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlist: StateFlow<List<WishlistItemEntity>> = repository.wishlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupons = repository.allCoupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vendor Data Isolation Flows
    val vendorProducts: StateFlow<List<ProductEntity>> = _selectedVendorId.flatMapLatest { id ->
        repository.getVendorProducts(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendorOrders: StateFlow<List<OrderEntity>> = _selectedVendorId.flatMapLatest { id ->
        repository.getVendorOrders(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Actions
    fun addToCart(product: ProductEntity, variant: String, price: Double) {
        viewModelScope.launch {
            repository.addToCart(
                CartItemEntity(
                    productId = product.id,
                    vendorId = product.vendorId,
                    productName = product.name,
                    variant = variant,
                    price = price,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
            )
        }
    }

    fun updateCartQty(id: Long, qty: Int) {
        viewModelScope.launch {
            if (qty > 0) {
                repository.updateCartQuantity(id, qty)
            } else {
                repository.removeFromCart(id)
            }
        }
    }

    fun removeFromCart(id: Long) {
        viewModelScope.launch {
            repository.removeFromCart(id)
        }
    }

    // Checkout & Orders
    fun placeOrder(
        customerName: String,
        customerMobile: String,
        address: String,
        itemsSummary: String,
        totalAmount: Double,
        paymentMethod: String,
        onOrderPlaced: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val vendorId = cartItems.value.firstOrNull()?.vendorId ?: 1L
            val order = OrderEntity(
                vendorId = vendorId,
                customerName = customerName,
                customerMobile = customerMobile,
                address = address,
                itemsSummary = itemsSummary,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                status = "ORDER_PLACED"
            )
            val orderId = repository.createOrder(order)
            repository.clearCart()
            onOrderPlaced(orderId)
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String, changedBy: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus, changedBy)
        }
    }

    // Vendor / Business Operations
    fun registerBusiness(business: BusinessEntity, onRegistered: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repository.registerBusiness(business)
            _selectedVendorId.value = newId
            onRegistered(newId)
        }
    }

    fun updateBusiness(business: BusinessEntity) {
        viewModelScope.launch {
            repository.updateBusiness(business)
        }
    }

    fun updateBusinessStatus(businessId: Long, status: String) {
        viewModelScope.launch {
            repository.updateBusinessStatus(businessId, status)
        }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    // Customer Profile & Address Operations
    fun updateProfile(name: String, mobile: String, email: String, defaultAddress: String) {
        viewModelScope.launch {
            repository.updateProfile(
                CustomerProfileEntity(
                    id = 1,
                    name = name,
                    mobile = mobile,
                    email = email,
                    defaultAddress = defaultAddress
                )
            )
        }
    }

    fun addAddress(title: String, addressLine: String, city: String, pincode: String) {
        viewModelScope.launch {
            repository.addAddress(
                AddressEntity(
                    title = title,
                    addressLine = addressLine,
                    city = city,
                    pincode = pincode,
                    isDefault = false
                )
            )
        }
    }

    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            repository.deleteAddress(id)
        }
    }

    // Wishlist Operations
    fun toggleWishlist(productId: Long) {
        viewModelScope.launch {
            val currentlyIn = wishlist.value.any { it.productId == productId }
            repository.toggleWishlist(productId, currentlyIn)
        }
    }
}
