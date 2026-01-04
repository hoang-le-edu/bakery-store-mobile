package com.dev.thecodecup.model.db.cart

import androidx.room.*
import java.util.UUID

@Entity(tableName = "offline_cart_actions")
data class OfflineCartAction(
    @PrimaryKey val localId: String = UUID.randomUUID().toString(),
    val productId: String,
    val cartItemId: Int? = null, // for update/delete actions
    val quantity: Int,
    val action: String, // "add", "update", "delete"
    val timestamp: String,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val deviceId: String,
    val isSynced: Boolean = false,
    val retryCount: Int = 0,
    val lastError: String? = null
)

enum class CartAction(val value: String) {
    ADD("add"),
    UPDATE("update"),
    DELETE("delete")
}