package com.dev.thecodecup.model.db.order

import androidx.room.*

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE isHistory = 0 ORDER BY id DESC")
    suspend fun getOngoingOrders(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE isHistory = 1 ORDER BY id DESC")
    suspend fun getHistoryOrders(): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE isHistory = 0")
    suspend fun clearOngoingOrders()

    @Query("DELETE FROM orders WHERE isHistory = 1")
    suspend fun clearHistoryOrders()

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Int): OrderEntity?
}
