package com.dev.thecodecup.model.db.cart

import androidx.room.*
import com.dev.thecodecup.model.item.CoffeeItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Embedded(prefix = "coffee_") val coffee: CoffeeItem,
    val shot: ShotLevel = ShotLevel.SINGLE,
    val size: CoffeeSize = CoffeeSize.MEDIUM,
    val ice: IceLevel = IceLevel.NORMAL,
    val haveIced: Boolean = false,
    val quantity: Int = 1,
    val point: Int = 12
)


enum class ShotLevel { SINGLE, DOUBLE }
enum class CoffeeSize { SMALL, MEDIUM, LARGE }
enum class IceLevel { LESS, NORMAL, EXTRA }