package com.dev.thecodecup.model.db.cart

import android.content.Context
import android.util.Log
import com.dev.thecodecup.model.network.api.*
import com.dev.thecodecup.model.auth.AuthManager
import com.dev.thecodecup.utils.DeviceManager
import com.dev.thecodecup.utils.NetworkStateManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class OfflineCartManager(
    private val context: Context,
    private val cartItemDao: CartItemDao,
    private val offlineActionDao: OfflineCartActionDao,
    private val bakeryApiService: BakeryApiService
) {
    companion object {
        private const val TAG = "OfflineCartManager"
        private const val MAX_RETRY_COUNT = 3
    }
    
    private val networkStateManager = NetworkStateManager(context)
    private val deviceId = DeviceManager.getDeviceId(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Public flows for UI observation
    private val _cartItems = MutableStateFlow<List<CartItemEntity>>(emptyList())
    val cartItems: StateFlow<List<CartItemEntity>> = _cartItems
    
    private val _pendingActionsCount = MutableStateFlow(0)
    val pendingActionsCount: StateFlow<Int> = _pendingActionsCount
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus
    
    init {
        networkStateManager.startListening()
        setupNetworkListener()
        loadCartItems()
        loadPendingActionsCount()
    }
    
    private fun setupNetworkListener() {
        scope.launch {
            networkStateManager.isOnline
                .collect { isOnline ->
                    if (isOnline) {
                        Log.d(TAG, "Network available - starting sync")
                        syncAllPendingActions()
                    } else {
                        Log.d(TAG, "Network lost - entering offline mode")
                    }
                }
        }
    }
    
    private fun loadCartItems() {
        scope.launch {
            cartItemDao.getActiveItemsFlow()
                .collect { items ->
                    _cartItems.value = items
                }
        }
    }
    
    private fun loadPendingActionsCount() {
        scope.launch {
            offlineActionDao.getPendingActionsFlow()
                .collect { actions ->
                    _pendingActionsCount.value = actions.size
                }
        }
    }
    
    // ==================== Public API Methods ====================
    
    suspend fun addItem(productId: String, quantity: Int, unitPrice: Double = 0.0): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = getCurrentTimestamp()
                val totalPrice = unitPrice * quantity
                
                // Create offline action
                val action = OfflineCartAction(
                    productId = productId,
                    quantity = quantity,
                    action = CartAction.ADD.value,
                    timestamp = timestamp,
                    unitPrice = unitPrice,
                    totalPrice = totalPrice,
                    deviceId = deviceId
                )
                
                // Save action locally
                offlineActionDao.insertAction(action)
                
                // Update local cart immediately for UX
                updateLocalCartForAddAction(productId, quantity, unitPrice)
                
                Log.d(TAG, "Added item to offline queue: $productId, qty: $quantity")
                
                // Try sync if online
                if (networkStateManager.isOnline.value) {
                    syncAction(action)
                }
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add item", e)
                false
            }
        }
    }
    
    suspend fun updateItemQuantity(cartItemId: Int, quantity: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cartItem = cartItemDao.getItemById(cartItemId)
                if (cartItem == null) {
                    Log.w(TAG, "Cart item not found: $cartItemId")
                    return@withContext false
                }
                
                val timestamp = getCurrentTimestamp()
                val totalPrice = cartItem.unitPrice * quantity
                
                val action = OfflineCartAction(
                    productId = cartItem.productId,
                    cartItemId = cartItemId,
                    quantity = quantity,
                    action = CartAction.UPDATE.value,
                    timestamp = timestamp,
                    unitPrice = cartItem.unitPrice,
                    totalPrice = totalPrice,
                    deviceId = deviceId
                )
                
                offlineActionDao.insertAction(action)
                
                // Update local cart
                val updatedItem = cartItem.copy(
                    quantity = quantity,
                    totalPrice = totalPrice,
                    lastUpdated = timestamp
                )
                cartItemDao.update(updatedItem)
                
                Log.d(TAG, "Updated item in offline queue: $cartItemId, qty: $quantity")
                
                if (networkStateManager.isOnline.value) {
                    syncAction(action)
                }
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update item", e)
                false
            }
        }
    }
    
    suspend fun removeItem(cartItemId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cartItem = cartItemDao.getItemById(cartItemId)
                if (cartItem == null) {
                    Log.w(TAG, "Cart item not found: $cartItemId")
                    return@withContext false
                }
                
                val timestamp = getCurrentTimestamp()
                
                val action = OfflineCartAction(
                    productId = cartItem.productId,
                    cartItemId = cartItemId,
                    quantity = 0,
                    action = CartAction.DELETE.value,
                    timestamp = timestamp,
                    unitPrice = cartItem.unitPrice,
                    totalPrice = 0.0,
                    deviceId = deviceId
                )
                
                offlineActionDao.insertAction(action)
                
                // Remove from local cart
                cartItemDao.markAsDeleted(cartItemId)
                
                Log.d(TAG, "Removed item from offline queue: $cartItemId")
                
                if (networkStateManager.isOnline.value) {
                    syncAction(action)
                }
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove item", e)
                false
            }
        }
    }
    
    suspend fun refreshCartFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthManager.getIdTokenOrNull()
                if (token == null) {
                    Log.w(TAG, "No auth token available")
                    return@withContext false
                }
                
                _syncStatus.value = SyncStatus.Syncing
                
                val response = bakeryApiService.getCart("Bearer $token", deviceId)
                
                if (response.isSuccessful) {
                    val cartData = response.body()?.data
                    if (cartData != null) {
                        updateLocalCartFromServer(cartData)
                        Log.d(TAG, "Successfully refreshed cart from server")
                        _syncStatus.value = SyncStatus.Success
                        return@withContext true
                    }
                }
                
                Log.w(TAG, "Failed to refresh cart: ${response.code()}")
                _syncStatus.value = SyncStatus.Error("Failed to refresh cart")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing cart", e)
                _syncStatus.value = SyncStatus.Error(e.message ?: "Unknown error")
                false
            }
        }
    }
    
    // ==================== Sync Logic ====================
    
    private suspend fun syncAllPendingActions() {
        withContext(Dispatchers.IO) {
            try {
                _syncStatus.value = SyncStatus.Syncing
                
                val pendingActions = offlineActionDao.getPendingActions()
                Log.d(TAG, "Syncing ${pendingActions.size} pending actions")
                
                for (action in pendingActions) {
                    syncAction(action)
                    delay(100) // Small delay between requests
                }
                
                // After all actions synced, refresh cart state
                refreshCartFromServer()
                
                _syncStatus.value = SyncStatus.Success
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
                _syncStatus.value = SyncStatus.Error(e.message ?: "Sync failed")
            }
        }
    }
    
    private suspend fun syncAction(action: OfflineCartAction) {
        try {
            val token = AuthManager.getIdTokenOrNull()
            if (token == null) {
                Log.w(TAG, "No auth token for sync")
                return
            }
            
            val response = when (action.action) {
                CartAction.ADD.value -> {
                    bakeryApiService.addCartItem(
                        "Bearer $token", 
                        deviceId,
                        AddCartItemRequest(action.productId, action.quantity)
                    )
                }
                CartAction.UPDATE.value -> {
                    if (action.cartItemId != null) {
                        bakeryApiService.updateCartItem(
                            "Bearer $token",
                            deviceId,
                            action.cartItemId.toString(),
                            UpdateCartItemRequest(action.quantity)
                        )
                    } else null
                }
                CartAction.DELETE.value -> {
                    if (action.cartItemId != null) {
                        bakeryApiService.removeCartItem(
                            "Bearer $token",
                            deviceId,
                            action.cartItemId.toString()
                        )
                    } else null
                }
                else -> null
            }
            
            if (response != null) {
                if (response.isSuccessful) {
                    // Mark as synced and remove
                    offlineActionDao.deleteActionById(action.localId)
                    Log.d(TAG, "Successfully synced action: ${action.action} for ${action.productId}")
                } else if (response.code() == 409) {
                    // Conflict - remove from queue (server wins)
                    offlineActionDao.deleteActionById(action.localId)
                    Log.w(TAG, "Conflict detected for action ${action.localId}, removing from queue")
                } else {
                    // Other errors - increment retry count
                    handleSyncError(action, "HTTP ${response.code()}")
                }
            }
        } catch (e: Exception) {
            handleSyncError(action, e.message ?: "Network error")
        }
    }
    
    private suspend fun handleSyncError(action: OfflineCartAction, error: String) {
        if (action.retryCount >= MAX_RETRY_COUNT) {
            // Max retries reached - remove action
            offlineActionDao.deleteActionById(action.localId)
            Log.w(TAG, "Max retries reached for action ${action.localId}, removing")
        } else {
            // Increment retry count
            offlineActionDao.incrementRetryCount(action.localId, error)
            Log.d(TAG, "Retry count incremented for action ${action.localId}: ${action.retryCount + 1}")
        }
    }
    
    // ==================== Local Cart Management ====================
    
    private suspend fun updateLocalCartForAddAction(productId: String, quantity: Int, unitPrice: Double) {
        // Check if item already exists
        val existingItem = cartItemDao.getItemByProductId(productId)
        
        if (existingItem != null) {
            // Update existing item
            val newQuantity = existingItem.quantity + quantity
            val updatedItem = existingItem.copy(
                quantity = newQuantity,
                totalPrice = unitPrice * newQuantity,
                lastUpdated = getCurrentTimestamp()
            )
            cartItemDao.update(updatedItem)
        } else {
            // Create new local item (with temporary ID)
            val newItem = CartItemEntity(
                id = 0, // Auto-generate ID
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = unitPrice * quantity,
                status = CartItemStatus.ACTIVE.value,
                deviceId = deviceId,
                lastUpdated = getCurrentTimestamp(),
                productName = "Loading..." // Will be updated from server
            )
            cartItemDao.insert(newItem)
        }
    }
    
    private suspend fun updateLocalCartFromServer(serverCart: OfflineCartData) {
        // Clear current active items
        cartItemDao.clearActiveItems()
        
        // Insert server items
        val localItems = serverCart.cart_items.map { serverItem ->
            CartItemEntity(
                id = serverItem.id.toIntOrNull() ?: 0,
                productId = serverItem.product_id,
                quantity = serverItem.quantity,
                unitPrice = serverItem.unit_price,
                totalPrice = serverItem.total_price,
                status = serverItem.status,
                deviceId = serverItem.device_id,
                lastUpdated = serverItem.last_updated,
                serverTimestamp = serverCart.server_timestamp,
                productName = serverItem.product?.name,
                productImage = serverItem.product?.image,
                isProductAvailable = serverItem.product?.is_available ?: true
            )
        }
        
        cartItemDao.insertAll(localItems)
        Log.d(TAG, "Updated local cart with ${localItems.size} items from server")
    }
    
    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return format.format(Date())
    }
    
    fun cleanup() {
        networkStateManager.stopListening()
        scope.cancel()
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
