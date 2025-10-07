package com.dev.thecodecup.model.db.cart

import android.content.Context
import androidx.room.Room
import com.dev.thecodecup.model.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository private constructor(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "cart-db"
    ).build()
    private val cartDao = db.cartItemDao()

    suspend fun getAllCartItems(): List<CartItemEntity> = withContext(Dispatchers.IO) {
        cartDao.getAll()
    }

    suspend fun insertCartItem(item: CartItemEntity) = withContext(Dispatchers.IO) {
        cartDao.insert(item)
    }

    suspend fun deleteCartItem(item: CartItemEntity) = withContext(Dispatchers.IO) {
        cartDao.delete(item)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearAll()
    }

    companion object {
        @Volatile private var instance: CartRepository? = null
        fun getInstance(context: Context): CartRepository =
            instance ?: synchronized(this) {
                instance ?: CartRepository(context).also { instance = it }
            }
    }
}
