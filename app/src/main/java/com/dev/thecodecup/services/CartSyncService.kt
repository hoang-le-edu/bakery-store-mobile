package com.dev.thecodecup.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.dev.thecodecup.model.db.AppDatabase
import com.dev.thecodecup.model.db.cart.OfflineCartManager
import com.dev.thecodecup.model.network.api.BakeryApiService
import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.utils.NetworkStateManager
import kotlinx.coroutines.*

class CartSyncService : Service() {
    companion object {
        private const val TAG = "CartSyncService"
        private const val ACTION_START_SYNC = "ACTION_START_SYNC"
        private const val ACTION_STOP_SYNC = "ACTION_STOP_SYNC"
        
        fun startSync(context: Context) {
            val intent = Intent(context, CartSyncService::class.java)
            intent.action = ACTION_START_SYNC
            context.startService(intent)
        }
        
        fun stopSync(context: Context) {
            val intent = Intent(context, CartSyncService::class.java)
            intent.action = ACTION_STOP_SYNC
            context.startService(intent)
        }
    }
    
    private var serviceScope: CoroutineScope? = null
    private var offlineCartManager: OfflineCartManager? = null
    private var networkStateManager: NetworkStateManager? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CartSyncService created")
        
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        
        // Initialize dependencies
        val database = AppDatabase.getDatabase(this)
        val apiService = NetworkModule.apiService as BakeryApiService
        
        offlineCartManager = OfflineCartManager(
            context = this,
            cartItemDao = database.cartItemDao(),
            offlineActionDao = database.offlineCartActionDao(),
            bakeryApiService = apiService
        )
        
        networkStateManager = NetworkStateManager(this)
        
        startNetworkMonitoring()
    }
    
    private fun startNetworkMonitoring() {
        serviceScope?.launch {
            networkStateManager?.let { manager ->
                manager.startListening()
                
                manager.isOnline
                    .collect { isOnline ->
                        if (isOnline) {
                            Log.d(TAG, "Network available - triggering background sync")
                            triggerSync()
                        } else {
                            Log.d(TAG, "Network lost - waiting for connection")
                        }
                    }
            }
        }
    }
    
    private suspend fun triggerSync() {
        try {
            offlineCartManager?.refreshCartFromServer()
            Log.d(TAG, "Background sync completed")
        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SYNC -> {
                Log.d(TAG, "Starting sync monitoring")
                // Already started in onCreate
            }
            ACTION_STOP_SYNC -> {
                Log.d(TAG, "Stopping sync monitoring")
                stopSelf()
            }
        }
        
        return START_STICKY // Restart if killed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "CartSyncService destroyed")
        
        networkStateManager?.stopListening()
        offlineCartManager?.cleanup()
        serviceScope?.cancel()
        
        serviceScope = null
        offlineCartManager = null
        networkStateManager = null
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }
}