package com.dev.thecodecup.model.db.order

import androidx.room.*
import com.dev.thecodecup.model.db.cart.CartItemEntity

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Embedded(prefix = "cart_") val cartItem: CartItemEntity,
    val location: String,
    val orderTime: String,
    val isHistory: Boolean = false
)

