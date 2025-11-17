package com.dev.thecodecup.model.network.repository

import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.model.network.NetworkModule.apiService
import com.dev.thecodecup.model.network.dto.ProductByIdDto
import com.dev.thecodecup.model.network.dto.ProductDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRemoteRepository {
    
    private val apiService = NetworkModule.apiService
    
    /**
     * Get all products with optional filters
     * @param limit Maximum number of products to return
     * @param searchText Search query for product name
     * @param categoryId Filter by category ID (use "all" for all categories)
     * @return Result containing list of products or error
     */
    suspend fun getAllProducts(
        limit: Int? = null,
        searchText: String? = null,
        categoryId: String? = "all"
    ): Result<List<ProductDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllProducts(limit, searchText, categoryId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Use getAllProducts() helper to flatten category structure
                    val products = body.getAllProducts()

                    // <<< THÊM LOG ĐỂ KIỂM TRA SỐ LƯỢNG SẢN PHẨM TRONG REPOSITORY >>>
                    android.util.Log.d("Repo", "getAllProducts: Kích thước danh sách sản phẩm được làm phẳng: ${products.size}")

                    Result.success(products)
                } else {
                    android.util.Log.e("Repo", "getAllProducts: Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("Repo", "getAllProducts: Exception", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get product by ID
     * @param productId Product ID to retrieve
     * @return Result containing product details or error
     */
    suspend fun getProductById(productId: String): Result<ProductByIdDto?> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProductById(productId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Product not found"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }



    /**
     * Search products by query
     * @param query Search query
     * @param limit Maximum number of results
     * @return Result containing list of matching products or error
     */
    suspend fun searchProducts(
        query: String,
        limit: Int? = null
    ): Result<List<ProductDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchProducts(query, limit)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.getAllProducts())
                } else {
                    Result.failure(Exception("Search failed"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        @Volatile
        private var instance: ProductRemoteRepository? = null
        
        fun getInstance(): ProductRemoteRepository =
            instance ?: synchronized(this) {
                instance ?: ProductRemoteRepository().also { instance = it }
            }
    }

}

