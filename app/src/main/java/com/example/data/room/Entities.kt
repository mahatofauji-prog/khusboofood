package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long = 1, // Associated Business ID for Data Isolation
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
    val isEnabled: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val vendorId: Long = 1,
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
    val customerName: String,
    val customerMobile: String,
    val address: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val status: String,
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryPartnerId: Long = 1
)

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ownerName: String,
    val ownerMobile: String,
    val ownerEmail: String,
    val category: String,
    val description: String,
    val logoUrl: String = "",
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    val mapsUrl: String = "",
    val openingTime: String = "09:00 AM",
    val closingTime: String = "10:00 PM",
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
    val defaultAddress: String = "House 42, Main Market Road, Near Temple, Sweets City"
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // e.g., "Home", "Office"
    val addressLine: String,
    val city: String,
    val pincode: String,
    val isDefault: Boolean = false
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
