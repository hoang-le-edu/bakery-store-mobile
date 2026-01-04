package com.dev.thecodecup.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.thecodecup.model.db.cart.CartItemDao
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.Converters
import com.dev.thecodecup.model.db.cart.OfflineCartAction
import com.dev.thecodecup.model.db.cart.OfflineCartActionDao
import com.dev.thecodecup.model.db.order.OrderDao
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.user.UserDao
import com.dev.thecodecup.model.db.user.UserEntity

@Database(
    entities = [CartItemEntity::class, OfflineCartAction::class, OrderEntity::class, UserEntity::class],
    version = 4, // Increment version for new entity
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDao
    abstract fun offlineCartActionDao(): OfflineCartActionDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bakery_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
