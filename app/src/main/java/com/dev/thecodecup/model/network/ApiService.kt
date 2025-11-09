package com.dev.thecodecup.model.network

import com.dev.thecodecup.model.network.dto.CategoriesResponse
import com.dev.thecodecup.model.network.dto.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    
    /**
     * Get all products with optional filters
     * Example: /api/customer/products/all?limit=10&searchText=coffee&category_id=123
     */
    @GET("api/customer/products/all")
    suspend fun getAllProducts(
        @Query("limit") limit: Int? = null,
        @Query("searchText") searchText: String? = null,
        @Query("category_id") categoryId: String? = "all"
    ): Response<ProductsResponse>
    
    /**
     * Get all categories
     * Example: /api/customer/categories
     */
    @GET("api/customer/products/all")
    suspend fun getAllCategories(): Response<ProductsResponse>
    
    /**
     * Get product by ID
     * Example: /api/customer/products/{id}
     */
    @GET("api/customer/products/{id}")
    suspend fun getProductById(
        @retrofit2.http.Path("id") productId: String
    ): Response<ProductsResponse>
    
    /**
     * Search products
     * Example: /api/customer/products/search?query=coffee
     */
    @GET("api/customer/products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null
    ): Response<ProductsResponse>
}

