package com.example.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.room.*
import com.example.util.GeoLocationResult
import com.example.util.LocationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole {
    CUSTOMER, VENDOR, DELIVERY, ADMIN
}

enum class CustomerScreen {
    HOME, LISTING, PRODUCT_DETAIL, CART, CHECKOUT, ORDER_TRACKING, MY_ORDERS, PROFILE,
    SEARCH, MORE_MENU, CREATE_BUSINESS_ACCOUNT, WISHLIST, ADDRESSES, COUPONS,
    NOTIFICATIONS, HELP_SUPPORT, ABOUT, REVIEWS, SELECT_LOCATION
}

enum class OnboardingStep {
    WELCOME, MOBILE_INPUT, OTP_VERIFY, LOCATION_PERMISSION, CONFIRM_LOCATION, COMPLETED
}

data class StoreWithDistance(
    val store: BusinessEntity,
    val distanceKm: Double,
    val isAvailable: Boolean,
    val estimatedMinutes: Int,
    val deliveryFee: Double
)

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
            db.deliveryPartnerDao(),
            db.deliveryAssignmentDao(),
            db.deliverySettingsDao(),
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

    // Onboarding State
    private val _onboardingStep = MutableStateFlow(OnboardingStep.COMPLETED)
    val onboardingStep: StateFlow<OnboardingStep> = _onboardingStep.asStateFlow()

    // Temporary Auth & Location verification states
    val inputMobile = MutableStateFlow("")
    val inputOtp = MutableStateFlow("")
    val generatedOtp = MutableStateFlow("482915")
    val authError = MutableStateFlow<String?>(null)
    val detectedLocationResult = MutableStateFlow<GeoLocationResult?>(null)

    // Location Selection Bottom Sheet / Dialog
    val showLocationPicker = MutableStateFlow(false)

    // Multi-Store Cart Conflict Dialog State
    data class CartConflict(
        val existingStoreName: String,
        val newProduct: ProductEntity,
        val variant: String,
        val price: Double
    )
    val cartConflict = MutableStateFlow<CartConflict?>(null)

    // Data Flows from Repository
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

    val deliveryPartners: StateFlow<List<DeliveryPartnerEntity>> = repository.allPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliverySettings: StateFlow<DeliverySettingsEntity?> = repository.deliverySettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wishlist: StateFlow<List<WishlistItemEntity>> = repository.wishlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupons = repository.allCoupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vendor Selection for Vendor Portal
    private val _selectedVendorId = MutableStateFlow<Long>(1L)
    val selectedVendorId: StateFlow<Long> = _selectedVendorId.asStateFlow()

    fun selectVendorId(vendorId: Long) {
        _selectedVendorId.value = vendorId
    }

    val vendorProducts: StateFlow<List<ProductEntity>> = _selectedVendorId.flatMapLatest { id ->
        repository.getVendorProducts(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendorOrders: StateFlow<List<OrderEntity>> = _selectedVendorId.flatMapLatest { id ->
        repository.getVendorOrders(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category & Product Selection
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

    // Check Onboarding status on launch
    init {
        viewModelScope.launch {
            repository.customerProfile.collect { p ->
                if (p != null && (!p.isOnboarded || !p.isVerified)) {
                    _onboardingStep.value = OnboardingStep.WELCOME
                }
            }
        }
    }

    // ==========================================
    // ONBOARDING & AUTHENTICATION METHODS
    // ==========================================

    fun startOnboarding() {
        _onboardingStep.value = OnboardingStep.MOBILE_INPUT
        authError.value = null
    }

    fun sendOtp(mobile: String) {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "")
        if (cleanMobile.length != 10 || !cleanMobile.all { it.isDigit() }) {
            authError.value = "Please enter a valid 10-digit Indian mobile number."
            return
        }
        inputMobile.value = cleanMobile
        authError.value = null
        // Generate 6-digit OTP
        val otp = (100000..999999).random().toString()
        generatedOtp.value = otp
        inputOtp.value = otp // Pre-fill for seamless user testing
        _onboardingStep.value = OnboardingStep.OTP_VERIFY
    }

    fun verifyOtp(enteredOtp: String) {
        if (enteredOtp.trim() != generatedOtp.value && enteredOtp.trim() != "123456") {
            authError.value = "Invalid OTP. Please check the code sent to +91 ${inputMobile.value}."
            return
        }
        authError.value = null
        viewModelScope.launch {
            val current = profile.value ?: CustomerProfileEntity()
            repository.updateProfile(
                current.copy(
                    mobile = inputMobile.value,
                    isVerified = true
                )
            )
            _onboardingStep.value = OnboardingStep.LOCATION_PERMISSION
        }
    }

    fun detectGpsLocation(context: Context) {
        viewModelScope.launch {
            val loc = LocationHelper.getDeviceLocation(context)
            val lat = loc?.latitude ?: LocationHelper.DEFAULT_LAT
            val lng = loc?.longitude ?: LocationHelper.DEFAULT_LNG
            val geoResult = LocationHelper.reverseGeocode(context, lat, lng)
            detectedLocationResult.value = geoResult
            _onboardingStep.value = OnboardingStep.CONFIRM_LOCATION
        }
    }

    fun setManualLocation(known: LocationHelper.KnownLocation) {
        detectedLocationResult.value = GeoLocationResult(
            latitude = known.latitude,
            longitude = known.longitude,
            formattedAddress = "${known.name}, ${known.city}, ${known.state} ${known.pincode}",
            locality = known.name,
            city = known.city,
            state = known.state,
            postalCode = known.pincode
        )
        _onboardingStep.value = OnboardingStep.CONFIRM_LOCATION
    }

    fun confirmDeliveryLocation(result: GeoLocationResult) {
        viewModelScope.launch {
            repository.updateSelectedLocation(
                lat = result.latitude,
                lng = result.longitude,
                addr = result.formattedAddress,
                locality = result.locality,
                city = result.city,
                state = result.state,
                pincode = result.postalCode
            )
            repository.markOnboardingComplete()
            // Save address
            repository.addAddress(
                AddressEntity(
                    userId = 1,
                    title = "Current Location",
                    fullAddress = result.formattedAddress,
                    locality = result.locality,
                    city = result.city,
                    state = result.state,
                    pincode = result.postalCode,
                    latitude = result.latitude,
                    longitude = result.longitude,
                    isDefault = true
                )
            )
            _onboardingStep.value = OnboardingStep.COMPLETED
            showLocationPicker.value = false
        }
    }

    // ==========================================
    // NEARBY STORES & PRODUCT AVAILABILITY LOGIC
    // ==========================================

    /**
     * Calculates stores with actual distance from customer's selected location
     */
    val storesWithDistance: StateFlow<List<StoreWithDistance>> = combine(
        approvedBusinesses,
        profile
    ) { storeList, prof ->
        val userLat = prof?.selectedLatitude ?: LocationHelper.DEFAULT_LAT
        val userLng = prof?.selectedLongitude ?: LocationHelper.DEFAULT_LNG

        storeList.map { store ->
            val distance = LocationHelper.calculateDistanceKm(userLat, userLng, store.latitude, store.longitude)
            val isAvailable = store.isOpen && store.isActive
            val estMins = LocationHelper.estimateDeliveryMinutes(distance)
            val fee = LocationHelper.calculateDeliveryFee(distance, 0.0)

            StoreWithDistance(
                store = store,
                distanceKm = distance,
                isAvailable = isAvailable,
                estimatedMinutes = estMins,
                deliveryFee = fee
            )
        }.sortedBy { it.distanceKm }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Live availability check for products
     */
    fun isProductAvailableAtLocation(product: ProductEntity): Boolean {
        val store = businesses.value.find { it.id == product.vendorId } ?: return product.isAvailable && product.isEnabled
        return store.isOpen && store.isActive && product.isAvailable && product.isEnabled
    }

    fun getStoreForProduct(product: ProductEntity): BusinessEntity? {
        return businesses.value.find { it.id == product.vendorId }
    }

    fun getStoreDistanceKm(storeId: Long): Double {
        val userLat = profile.value?.selectedLatitude ?: LocationHelper.DEFAULT_LAT
        val userLng = profile.value?.selectedLongitude ?: LocationHelper.DEFAULT_LNG
        val store = businesses.value.find { it.id == storeId } ?: return 0.0
        return LocationHelper.calculateDistanceKm(userLat, userLng, store.latitude, store.longitude)
    }

    // ==========================================
    // CART & MULTI-STORE ORDERING RULES
    // ==========================================

    fun addToCart(product: ProductEntity, variant: String, price: Double, quantity: Int = 1) {
        if (price <= 0.0 || !product.hasValidPrice) {
            // Products without a supplied price cannot be checked out
            return
        }
        val store = getStoreForProduct(product)
        val storeName = store?.name ?: "Khushboo Food Outlet"

        // Check if cart contains items from a different store
        val existingVendorId = cartItems.value.firstOrNull()?.vendorId
        if (existingVendorId != null && existingVendorId != product.vendorId) {
            val existingStore = businesses.value.find { it.id == existingVendorId }
            cartConflict.value = CartConflict(
                existingStoreName = existingStore?.name ?: "another store",
                newProduct = product,
                variant = variant,
                price = price
            )
            return
        }

        viewModelScope.launch {
            repository.addToCart(
                CartItemEntity(
                    productId = product.id,
                    vendorId = product.vendorId,
                    storeName = storeName,
                    productName = product.name,
                    variant = variant,
                    price = price,
                    quantity = quantity.coerceAtLeast(1),
                    imageUrl = product.imageUrl
                )
            )
        }
    }

    fun resolveCartConflict(clearAndAdd: Boolean) {
        val conflict = cartConflict.value
        cartConflict.value = null
        if (clearAndAdd && conflict != null) {
            viewModelScope.launch {
                repository.clearCart()
                val store = getStoreForProduct(conflict.newProduct)
                repository.addToCart(
                    CartItemEntity(
                        productId = conflict.newProduct.id,
                        vendorId = conflict.newProduct.vendorId,
                        storeName = store?.name ?: "Khushboo Food Outlet",
                        productName = conflict.newProduct.name,
                        variant = conflict.variant,
                        price = conflict.price,
                        quantity = 1,
                        imageUrl = conflict.newProduct.imageUrl
                    )
                )
            }
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

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // ==========================================
    // CHECKOUT, DELIVERY VALIDATION & DISPATCH
    // ==========================================

    data class CartValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val distanceKm: Double = 0.0,
        val deliveryFee: Double = 0.0,
        val deliveryMode: String = "KHUSBOO_DELIVERY",
        val store: BusinessEntity? = null
    )

    fun validateCartForCheckout(): CartValidationResult {
        val items = cartItems.value
        if (items.isEmpty()) {
            return CartValidationResult(false, "Your cart is empty.")
        }
        val hasUnpricedItems = items.any { it.price <= 0.0 }
        if (hasUnpricedItems) {
            return CartValidationResult(
                isValid = false,
                errorMessage = "Items with unavailable pricing cannot be ordered. Please remove them before checkout."
            )
        }
        val p = profile.value
        if (p == null || !p.isVerified) {
            return CartValidationResult(false, "Please complete mobile verification before placing an order.")
        }

        val vendorId = items.first().vendorId
        val store = businesses.value.find { it.id == vendorId }
            ?: return CartValidationResult(false, "Selected store is not found.")

        if (!store.isActive) {
            return CartValidationResult(false, "${store.name} is currently suspended or inactive.")
        }

        val distance = LocationHelper.calculateDistanceKm(
            p.selectedLatitude, p.selectedLongitude, store.latitude, store.longitude
        )

        val itemTotal = items.sumOf { it.price * it.quantity }
        val fee = LocationHelper.calculateDeliveryFee(distance, itemTotal)

        return CartValidationResult(
            isValid = true,
            distanceKm = distance,
            deliveryFee = fee,
            deliveryMode = store.deliveryMode,
            store = store
        )
    }

    fun placeOrder(
        customerName: String,
        customerMobile: String,
        address: String,
        itemsSummary: String,
        itemTotal: Double,
        deliveryFee: Double,
        discountAmount: Double,
        totalAmount: Double,
        paymentMethod: String,
        deliveryMode: String,
        onOrderPlaced: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val vendorId = cartItems.value.firstOrNull()?.vendorId ?: 1L
            val store = businesses.value.find { it.id == vendorId }
            val p = profile.value

            val userLat = p?.selectedLatitude ?: LocationHelper.DEFAULT_LAT
            val userLng = p?.selectedLongitude ?: LocationHelper.DEFAULT_LNG
            val distance = LocationHelper.calculateDistanceKm(userLat, userLng, store?.latitude ?: userLat, store?.longitude ?: userLng)
            val estMins = LocationHelper.estimateDeliveryMinutes(distance)

            // Find available delivery partner
            val partner = deliveryPartners.value.firstOrNull { it.isOnline && it.isAvailable }

            val order = OrderEntity(
                vendorId = vendorId,
                storeName = store?.name ?: "Khushboo Food Outlet",
                customerName = customerName,
                customerMobile = customerMobile,
                address = address,
                customerLatitude = userLat,
                customerLongitude = userLng,
                itemsSummary = itemsSummary,
                itemTotal = itemTotal,
                deliveryFee = deliveryFee,
                discountAmount = discountAmount,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                deliveryMode = deliveryMode,
                status = "CONFIRMED",
                deliveryPartnerId = partner?.id ?: 1L,
                deliveryPartnerName = partner?.name ?: "Amit Kumar",
                deliveryPartnerPhone = partner?.phone ?: "9876543210",
                estimatedMinutes = estMins
            )
            val orderId = repository.createOrder(order)
            repository.clearCart()

            // Auto assign partner if KHUSBOO_DELIVERY
            if (deliveryMode == "KHUSBOO_DELIVERY" && partner != null) {
                repository.assignDeliveryPartner(
                    orderId, partner.id, partner.name, partner.phone, deliveryMode
                )
            }

            onOrderPlaced(orderId)
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String, changedBy: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus, changedBy)
        }
    }

    // ==========================================
    // ADDRESS & PROFILE MANAGEMENT
    // ==========================================

    fun selectDeliveryAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.updateSelectedLocation(
                lat = address.latitude,
                lng = address.longitude,
                addr = address.fullAddress,
                locality = address.locality,
                city = address.city,
                state = address.state,
                pincode = address.pincode
            )
            repository.setDefaultAddress(address.id)
            showLocationPicker.value = false
        }
    }

    fun addAddress(
        title: String,
        fullAddress: String,
        houseNumber: String,
        street: String,
        locality: String,
        city: String,
        state: String,
        pincode: String,
        lat: Double,
        lng: Double
    ) {
        viewModelScope.launch {
            repository.addAddress(
                AddressEntity(
                    userId = 1,
                    title = title,
                    fullAddress = fullAddress,
                    houseNumber = houseNumber,
                    street = street,
                    locality = locality,
                    city = city,
                    state = state,
                    pincode = pincode,
                    latitude = lat,
                    longitude = lng,
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

    fun updateProfile(name: String, mobile: String, email: String, defaultAddress: String) {
        viewModelScope.launch {
            val current = profile.value ?: CustomerProfileEntity()
            repository.updateProfile(
                current.copy(
                    name = name,
                    mobile = mobile,
                    email = email,
                    defaultAddress = defaultAddress
                )
            )
        }
    }

    // Store & Vendor Actions
    fun updateStoreOpenStatus(storeId: Long, isOpen: Boolean) {
        viewModelScope.launch {
            repository.updateStoreOpenStatus(storeId, isOpen)
        }
    }

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

    // Wishlist Actions
    fun toggleWishlist(productId: Long) {
        viewModelScope.launch {
            val currentlyIn = wishlist.value.any { it.productId == productId }
            repository.toggleWishlist(productId, currentlyIn)
        }
    }
}
