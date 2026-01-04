package com.dev.thecodecup.model.db.cart

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineCartActionDao {
    
    @Query("SELECT * FROM offline_cart_actions WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingActions(): List<OfflineCartAction>
    
    @Query("SELECT * FROM offline_cart_actions WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getPendingActionsFlow(): Flow<List<OfflineCartAction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: OfflineCartAction)
    
    @Update
    suspend fun updateAction(action: OfflineCartAction)
    
    @Delete
    suspend fun deleteAction(action: OfflineCartAction)
    
    @Query("DELETE FROM offline_cart_actions WHERE localId = :localId")
    suspend fun deleteActionById(localId: String)
    
    @Query("DELETE FROM offline_cart_actions WHERE isSynced = 1")
    suspend fun deleteSyncedActions()
    
    @Query("UPDATE offline_cart_actions SET isSynced = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: String)
    
    @Query("UPDATE offline_cart_actions SET retryCount = retryCount + 1, lastError = :error WHERE localId = :localId")
    suspend fun incrementRetryCount(localId: String, error: String?)
    
    @Query("DELETE FROM offline_cart_actions")
    suspend fun clearAll()
}