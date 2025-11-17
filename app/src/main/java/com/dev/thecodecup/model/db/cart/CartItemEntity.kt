package com.dev.thecodecup.model.db.cart

import androidx.room.*

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val imageResId: Int = 0,
    val imageUrl: String? = null,
    val shot: String = "Single",
    val size: String = "Medium",
    val ice: String = "Medium",
    val quantity: Int = 1,
    val point: Int = 12
)


enum class ShotLevel { SINGLE, DOUBLE }
enum class CoffeeSize { SMALL, MEDIUM, LARGE }
enum class IceLevel { LESS, NORMAL, EXTRA }