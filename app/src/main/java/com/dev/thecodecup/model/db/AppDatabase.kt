package com.dev.thecodecup.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.thecodecup.model.db.cart.CartItemDao
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.Converters
import com.dev.thecodecup.model.db.order.OrderDao
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.user.UserDao
import com.dev.thecodecup.model.db.user.UserEntity

@Database(
    entities = [CartItemEntity::class, OrderEntity::class, UserEntity::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
}
