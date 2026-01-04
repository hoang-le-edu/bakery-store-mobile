package com.dev.thecodecup.model.db.cart

import androidx.room.*

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: Int = 0, // Using server cart item ID or temp UUID
    val productId: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val status: String = "active", // active, checkout, deleted
    val deviceId: String = "",
    val lastUpdated: String = "",
    val serverTimestamp: String? = null,
    
    // Product details for offline display
    val productName: String? = null,
    val productImage: String? = null,
    val isProductAvailable: Boolean = true,
    
    // Legacy fields - keeping for compatibility
    val name: String = productName ?: "",
    val price: Double = unitPrice,
    val imageResId: Int = 0,
    val imageUrl: String? = productImage,
    val shot: String = "Single",
    val size: String = "Medium",
    val ice: String = "Medium",
    val point: Int = 12
)

enum class CartItemStatus(val value: String) {
    ACTIVE("active"),
    CHECKOUT("checkout"), 
    DELETED("deleted")
}

// Legacy enums - keeping for compatibility
enum class ShotLevel { SINGLE, DOUBLE }
enum class CoffeeSize { SMALL, MEDIUM, LARGE }
enum class IceLevel { LESS, NORMAL, EXTRA }