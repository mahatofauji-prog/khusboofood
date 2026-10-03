package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long = 1, // Associated Business / Store ID
    val name: String,
    val category: String,
    val subcategory: String = "",
    val price250g: Double = 0.0,
    val price500g: Double = 0.0,
    val price1kg: Double = 0.0,
    val priceStandard: Double = 0.0,
    val discountPrice: Double = 0.0,
    val description: String,
    val imageUrl: String = "",
    val stock: Int = 100,
    val sku: String = "KHU-PROD-001",
    val sizeWeight: String = "",
    val rating: Float = 4.8f,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isApproved: Boolean = true,
    val isEnabled: Boolean = true,
    val isAvailable: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val vendorId: Long = 1,
    val storeName: String = "KHUSHBOO FOOD Main Outlet",
    val productName: String,
    val variant: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long = 1,
    val storeName: String = "KHUSHBOO FOOD Main Outlet",
    val customerName: String,
    val customerMobile: String,
    val address: String,
    val customerLatitude: Double = 23.3322,
    val customerLongitude: Double = 86.3652,
    val itemsSummary: String,
    val itemTotal: Double = 0.0,
    val deliveryFee: Double = 25.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String,
    val deliveryMode: String = "KHUSBOO_DELIVERY", // "KHUSBOO_DELIVERY", "THIRD_PARTY_DELIVERY", "SELLER_SELF_DELIVERY", "CUSTOMER_PICKUP"
    val status: String = "CONFIRMED", // "PENDING_PAYMENT", "CONFIRMED", "STORE_ACCEPTED", "PREPARING", "READY_FOR_PICKUP", "DELIVERY_ASSIGNING", "DELIVERY_ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryPartnerId: Long = 1,
    val deliveryPartnerName: String = "Amit Kumar",
    val deliveryPartnerPhone: String = "9876543210",
    val estimatedMinutes: Int = 30
)

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // storeId
    val sellerId: Long = 1,
    val name: String, // storeName
    val ownerName: String,
    val ownerMobile: String, // phone
    val ownerEmail: String,
    val category: String,
    val description: String,
    val logoUrl: String = "",
    val address: String, // fullAddress
    val locality: String = "Purulia Town",
    val city: String = "Purulia",
    val state: String = "West Bengal",
    val pincode: String = "723101",
    val latitude: Double = 23.3322,
    val longitude: Double = 86.3652,
    val deliveryRadius: Double = 12.0, // in kilometers
    val deliveryMode: String = "KHUSBOO_DELIVERY", // "KHUSBOO_DELIVERY", "THIRD_PARTY_DELIVERY", "SELLER_SELF_DELIVERY", "CUSTOMER_PICKUP"
    val isOpen: Boolean = true,
    val isActive: Boolean = true,
    val mapsUrl: String = "",
    val openingTime: String = "08:00 AM",
    val closingTime: String = "10:30 PM",
    val gstNumber: String = "",
    val status: String = "APPROVED", // "PENDING_APPROVAL", "APPROVED", "REJECTED", "SUSPENDED"
    val rating: Float = 4.9f,
    val registrationDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_profiles")
data class CustomerProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "Rahul Sharma",
    val mobile: String = "6365839460",
    val email: String = "rahul.sharma@gmail.com",
    val photoUrl: String = "",
    val isVerified: Boolean = true,
    val isOnboarded: Boolean = true,
    val selectedLatitude: Double = 23.3322,
    val selectedLongitude: Double = 86.3652,
    val selectedAddress: String = "Main Market Road, Purulia Town, West Bengal",
    val selectedLocality: String = "Purulia Town",
    val selectedCity: String = "Purulia",
    val selectedState: String = "West Bengal",
    val selectedPostalCode: String = "723101",
    val defaultAddress: String = "House 42, Main Market Road, Purulia Town"
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val title: String = "Home", // label: "Home", "Work", "Other"
    val fullAddress: String,
    val houseNumber: String = "",
    val street: String = "",
    val locality: String = "",
    val city: String,
    val state: String = "West Bengal",
    val pincode: String,
    val latitude: Double = 23.3322,
    val longitude: Double = 86.3652,
    val contactName: String = "Rahul Sharma",
    val contactPhone: String = "6365839460",
    val deliveryInstructions: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_partners")
data class DeliveryPartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val profilePhoto: String = "",
    val vehicleType: String = "Bike",
    val vehicleNumber: String = "WB-56-1234",
    val currentLatitude: Double = 23.3322,
    val currentLongitude: Double = 86.3652,
    val isOnline: Boolean = true,
    val isAvailable: Boolean = true,
    val verificationStatus: String = "VERIFIED",
    val rating: Float = 4.9f,
    val totalDeliveries: Int = 142
)

@Entity(tableName = "delivery_assignments")
data class DeliveryAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val partnerId: Long,
    val deliveryMode: String = "KHUSBOO_DELIVERY",
    val assignedTime: Long = System.currentTimeMillis(),
    val acceptedTime: Long = 0L,
    val pickedUpTime: Long = 0L,
    val deliveredTime: Long = 0L,
    val status: String = "ASSIGNED"
)

@Entity(tableName = "delivery_settings")
data class DeliverySettingsEntity(
    @PrimaryKey val id: Long = 1,
    val baseDeliveryFee: Double = 25.0,
    val freeDeliveryThreshold: Double = 499.0,
    val perKmFee: Double = 6.0,
    val baseDistanceKm: Double = 2.0,
    val surgeFee: Double = 0.0,
    val khusbooDeliveryEnabled: Boolean = true,
    val thirdPartyDeliveryEnabled: Boolean = true,
    val sellerSelfDeliveryEnabled: Boolean = true,
    val customerPickupEnabled: Boolean = true
)

@Entity(tableName = "wishlist")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "order_status_history")
data class OrderStatusHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val previousStatus: String,
    val newStatus: String,
    val changedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val discountPercent: Int,
    val maxDiscount: Double,
    val minOrderValue: Double,
    val active: Boolean = true
)
