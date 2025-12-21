package com.dev.thecodecup.model.network

import com.dev.thecodecup.model.network.api.SuccessResponse
import com.dev.thecodecup.model.network.dto.AdminOrderDetailResponseDto
import com.dev.thecodecup.model.network.dto.AdminOrdersResponseDto
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto
import com.dev.thecodecup.model.network.dto.ApiResponse
import com.dev.thecodecup.model.network.dto.LoginResponseDto
import com.dev.thecodecup.model.network.dto.ProductByIdDto
import com.dev.thecodecup.model.network.dto.ProductsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    /**
     * Get all products with optional filters
     * Example: /api/customer/products/all?limit=10&searchText=coffee&category_id=123
     */
    @GET("customer/products/all")
    suspend fun getAllProducts(
        @Query("limit") limit: Int? = null,
        @Query("searchText") searchText: String? = null,
        @Query("category_id") categoryId: String? = "all"
    ): Response<ProductsResponse>
    
    /**
     * Get all categories
     * Example: /api/customer/categories
     */
    @GET("customer/products/all")
    suspend fun getAllCategories(): Response<ProductsResponse>
    
    /**
     * Get product by ID
     * Example: /api/customer/product/{id}
     */
    @GET("customer/product/{id}")
    suspend fun getProductById(
        @retrofit2.http.Path("id") productId: String
    ): Response<ApiResponse<ProductByIdDto>>
    
    /**
     * Search products
     * Example: /api/customer/products/search?query=coffee
     */
    @GET("customer/products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("limit") limit: Int? = null
    ): Response<ProductsResponse>

    /**
     * Login
     * Example: /api/auth/login
     */
    @POST("auth/login")
    fun login(
        @Body body: Map<String, String>
    ): Call<LoginResponseDto>

    /**
     * Get all categories
     * Example: /api/customer/categories
     */
    @GET("admin/products/all")
    fun getAdminProducts(
        @Query("limit") limit: Int? = null,
        @Query("searchText") searchText: String? = null,
        @Query("category_id") categoryId: String? = null
    ): Call<AdminProductsResponseDto>

    /**
     * Get all orders
     * Example: /api/admin/orders/all
     */
    @GET("admin/orders/all")
    fun getAdminOrders(): Call<AdminOrdersResponseDto>

    /**
     * Get order detail (admin)
     * Example: /api/admin/orders/detail/{id}
     */
    @GET("admin/orders/detail/{id}")
    fun getAdminOrderDetail(
        @Path("id") orderId: String
    ): Call<AdminOrderDetailResponseDto>

    /**
     * Update order status and record history
     * Example: /api/orders/status/{id} with body {"status":"In Progress"}
     */
    @POST("orders/status/{id}")
    fun updateOrderStatus(
        @Path("id") orderId: String,
        @Body body: Map<String, String>
    ): Call<SuccessResponse>

}

