package com.dev.thecodecup.model.db.order

import android.content.Context
import androidx.room.Room
import com.dev.thecodecup.model.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository private constructor(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "order-db"
    )
        .fallbackToDestructiveMigration() // Auto-delete and recreate DB on schema change
        .build()
    private val orderDao = db.orderDao()

    suspend fun getOngoingOrders(): List<OrderEntity> = withContext(Dispatchers.IO) {
        orderDao.getOngoingOrders()
    }

    suspend fun getHistoryOrders(): List<OrderEntity> = withContext(Dispatchers.IO) {
        orderDao.getHistoryOrders()
    }

    suspend fun insertOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun deleteOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.deleteOrder(order)
    }

    suspend fun updateOrder(order: OrderEntity) = orderDao.updateOrder(order)

    suspend fun getOrderById(orderId: Int): OrderEntity? = withContext(Dispatchers.IO) {
        orderDao.getOrderById(orderId)
    }

    companion object {
        @Volatile private var instance: OrderRepository? = null
        fun getInstance(context: Context): OrderRepository =
            instance ?: synchronized(this) {
                instance ?: OrderRepository(context).also { instance = it }
            }
    }
}
