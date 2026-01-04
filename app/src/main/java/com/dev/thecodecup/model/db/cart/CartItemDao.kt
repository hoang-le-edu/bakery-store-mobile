package com.dev.thecodecup.model.db.cart

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items WHERE status = 'active'")
    suspend fun getActiveItems(): List<CartItemEntity>
    
    @Query("SELECT * FROM cart_items WHERE status = 'active'")
    fun getActiveItemsFlow(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): CartItemEntity?
    
    @Query("SELECT * FROM cart_items WHERE productId = :productId AND status = 'active' LIMIT 1")
    suspend fun getItemByProductId(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CartItemEntity>)

    @Update
    suspend fun update(item: CartItemEntity)

    @Delete
    suspend fun delete(item: CartItemEntity)
    
    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)
    
    @Query("UPDATE cart_items SET status = 'deleted' WHERE id = :itemId")
    suspend fun markAsDeleted(itemId: Int)

    @Query("DELETE FROM cart_items WHERE status = 'active'")
    suspend fun clearActiveItems()
    
    @Query("DELETE FROM cart_items")
    suspend fun clearAll()
    
    @Query("SELECT SUM(totalPrice) FROM cart_items WHERE status = 'active'")
    suspend fun getTotalAmount(): Double?
    
    @Query("SELECT COUNT(*) FROM cart_items WHERE status = 'active'")
    suspend fun getTotalItemsCount(): Int
}

