package com.dev.thecodecup.model.db.order

import androidx.room.*
import com.dev.thecodecup.model.db.cart.CartItemEntity

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cart_name: String = "",
    val cart_price: Double = 0.0,
    val cart_imageResId: Int = 0,
    val cart_imageUrl: String? = null,
    val cart_shot: String = "Single",
    val cart_size: String = "Medium",
    val cart_ice: String = "Medium",
    val cart_quantity: Int = 1,
    val cart_point: Int = 12,
    val location: String,
    val orderTime: String,
    val isHistory: Boolean = false
) {
    // Helper function to convert to CartItemEntity (not stored in database)
    fun toCartItem(): CartItemEntity = CartItemEntity(
        id = 0,
        name = cart_name,
        price = cart_price,
        imageResId = cart_imageResId,
        imageUrl = cart_imageUrl,
        shot = cart_shot,
        size = cart_size,
        ice = cart_ice,
        quantity = cart_quantity,
        point = cart_point
    )
    
    companion object {
        // Helper factory method to create OrderEntity from CartItemEntity
        fun fromCartItem(
            cartItem: CartItemEntity,
            location: String,
            orderTime: String,
            isHistory: Boolean = false,
            id: Int = 0
        ) = OrderEntity(
            id = id,
            cart_name = cartItem.name,
            cart_price = cartItem.price,
            cart_imageResId = cartItem.imageResId,
            cart_imageUrl = cartItem.imageUrl,
            cart_shot = cartItem.shot,
            cart_size = cartItem.size,
            cart_ice = cartItem.ice,
            cart_quantity = cartItem.quantity,
            cart_point = cartItem.point,
            location = location,
            orderTime = orderTime,
            isHistory = isHistory
        )
    }
}

