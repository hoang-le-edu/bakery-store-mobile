package com.dev.thecodecup.model.network

import android.util.Log
import com.dev.thecodecup.model.network.repository.ProductRemoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Simple test class to verify API integration
 * 
 * Call this from your MainActivity or any screen to test the API:
 * ```
 * NetworkTest.testApiConnection()
 * ```
 */
object NetworkTest {
    
    private const val TAG = "NetworkTest"
    
    /**
     * Test API connection by fetching products
     */
    @JvmStatic
    fun testApiConnection() {
        Log.d(TAG, "Testing API connection...")
        
        val repository = ProductRemoteRepository.getInstance()
        
        CoroutineScope(Dispatchers.Main).launch {
            repository.getAllProducts(
                limit = 5,
                searchText = null,
                categoryId = "all"
            ).onSuccess { products ->
                Log.d(TAG, "✓ API Connection Successful!")
                Log.d(TAG, "✓ Retrieved ${products.size} products")
                
                products.forEach { product ->
                    Log.d(TAG, "  - ${product.productName}: $${product.price}")
                }
                
                if (products.isEmpty()) {
                    Log.w(TAG, "⚠ No products found in the response")
                }
            }.onFailure { exception ->
                Log.e(TAG, "✗ API Connection Failed!", exception)
                Log.e(TAG, "  Error: ${exception.message}")
                Log.e(TAG, "  Check your internet connection and API URL")
            }
        }
    }
    
    /**
     * Test with search
     */
    fun testSearch(query: String) {
        Log.d(TAG, "Testing search with query: '$query'")
        
        val repository = ProductRemoteRepository.getInstance()
        
        CoroutineScope(Dispatchers.Main).launch {
            repository.searchProducts(query)
                .onSuccess { products ->
                    Log.d(TAG, "✓ Search successful! Found ${products.size} products")
                    products.forEach { product ->
                        Log.d(TAG, "  - ${product.productName}")
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "✗ Search failed: ${exception.message}")
                }
        }
    }
}

