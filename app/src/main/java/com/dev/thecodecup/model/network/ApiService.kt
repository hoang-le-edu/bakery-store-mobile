package com.dev.thecodecup.model.network

import com.dev.thecodecup.model.network.api.SuccessResponse
import com.dev.thecodecup.model.network.dto.AdminOrderDetailResponseDto
import com.dev.thecodecup.model.network.dto.AdminOrdersResponseDto
import com.dev.thecodecup.model.network.dto.AdminCustomerDetailResponseDto
import com.dev.thecodecup.model.network.dto.AdminProductDetailDto
import com.dev.thecodecup.model.network.dto.AdminProductDto
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto
import com.dev.thecodecup.model.network.dto.ApiResponse
import com.dev.thecodecup.model.network.dto.CategoriesResponse
import com.dev.thecodecup.model.network.dto.LoginResponseDto
import com.dev.thecodecup.model.network.dto.ProductByIdDto
import com.dev.thecodecup.model.network.dto.ProductDetailResponseDto
import com.dev.thecodecup.model.network.dto.ProductOperationResponseDto
import com.dev.thecodecup.model.network.dto.ProductsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    /**
     * Get all products with optional filters
     * Example: /api/customer/products/all?limit=10&searchText=coffee&category_id=123&sort=best_seller
     * 
     * Parameters:
     * - sort: "best_seller" - Sort by highest sales
     * - limit: Limit number of products
     * - category_id: Filter by category
     * - searchText: Search by name
     * - min_price, max_price: Price range
     * - page_size: Items per page
     * - order: Sort by price (asc/desc)
     * 
     * When sort=best_seller, response includes total_sold field per product
     */
    @GET("customer/products/all")
    suspend fun getAllProducts(
        @Query("limit") limit: Int? = null,
        @Query("searchText") searchText: String? = null,
        @Query("category_id") categoryId: String? = "all",
        @Query("sort") sort: String? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("order") order: String? = null
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
        @Query("keysearch") keysearch: String? = null,
        @Query("status") status: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("page_size") pageSize: Int? = null
    ): Call<AdminProductsResponseDto>

    /**
     * Get admin product by id
     * Example: /api/admin/products/{id}
     */
    @GET("admin/products/{id}")
    fun getAdminProductById(
        @Path("id") productId: String
    ): Call<ApiResponse<AdminProductDetailDto>>

    /**
     * Get all orders
     * GET /api/admin/orders/all
     * 
     * Parameters:
     * - status: Draft, Wait For Approval, In Progress, Delivering, Completed, Cancelled
     * - payment_method: Banking, Cash, ""
     * - payment_status: pending, paid
     * - keysearch: Search by order_number or receiver_name
     * - order_total: 1=<100k, 2=100k-300k, 3=300k-500k, 4=>500k
     * - from_date, to_date: Date range (YYYY-MM-DD)
     * 
     * Default sorting: order_date DESC, then updated_at DESC
     * 
     * Example: /api/admin/orders/all?status=Completed&payment_method=Banking
     */
    @GET("admin/orders/all")
    fun getAdminOrders(
        @Query("status") status: String? = null,
        @Query("payment_method") paymentMethod: String? = null,
        @Query("payment_status") paymentStatus: String? = null,
        @Query("keysearch") keysearch: String? = null,
        @Query("order_total") orderTotal: Int? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("order_date") orderDate: String? = null
    ): Call<AdminOrdersResponseDto>

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

    /**
     * Get customer detail with orders
     * Example: /api/admin/orders/customerInfo/{id}
     */
    @GET("admin/orders/customerInfo/{id}")
    fun getAdminCustomerDetail(
        @Path("id") customerId: String
    ): Call<AdminCustomerDetailResponseDto>

    /**
     * Search admin orders with filters
     * GET /api/admin/orders/search
     * 
     * Deprecated: Use getAdminOrders() with query parameters instead
     * This endpoint is kept for backward compatibility
     */
    @GET("admin/orders/search")
    fun searchAdminOrders(
        @Query("order_id") orderId: String? = null,
        @Query("customer_name") customerName: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Call<AdminOrdersResponseDto>

    /**
     * Update admin product
     * Example: /api/admin/products/update/{id}
     */
    @POST("admin/products/update/{id}")
    fun updateAdminProduct(
        @Path("id") productId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Call<AdminProductDto>

    /**
     * Update admin order (payment status, order status, etc.)
     * Example: /api/admin/orders/update/{id}
     */
    @POST("admin/orders/update/{id}")
    fun updateAdminOrder(
        @Path("id") orderId: String,
        @Body body: Map<String, String>
    ): Call<SuccessResponse>

    /**
     * Get all categories for admin
     * Example: /api/admin/categories/all
     */
    @GET("admin/categories/all")
    fun getAdminCategories(): Call<CategoriesResponse>

    /**
     * Get admin product detail by id
     * Example: /api/admin/products/{id}
     */
    @GET("admin/products/{id}")
    fun getAdminProductDetail(
        @Path("id") productId: String
    ): Call<ProductDetailResponseDto>

    /**
     * Add new product with multipart/form-data
     * Example: /api/admin/products/add
     */
    @Multipart
    @POST("admin/products/add")
    fun addAdminProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("status") status: RequestBody,
        @Part("price") price: RequestBody,
        @Part("cost") cost: RequestBody,
        @Part("up_m_price") upMPrice: RequestBody,
        @Part("up_l_price") upLPrice: RequestBody,
        @Part("is_topping") isTopping: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part thumbnailImage: MultipartBody.Part?,
        @Part("categories_id[]") categoriesId: List<@JvmSuppressWildcards RequestBody>,
        @Part("toppings_id[]") toppingsId: List<@JvmSuppressWildcards RequestBody>?,
        @Part productDetailImages: List<MultipartBody.Part>?
    ): Call<ProductOperationResponseDto>

    /**
     * Update product with multipart/form-data
     * Example: /api/admin/products/update/{id}
     */
    @Multipart
    @POST("admin/products/update/{id}")
    fun updateAdminProductMultipart(
        @Path("id") productId: String,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part("price") price: RequestBody?,
        @Part("cost") cost: RequestBody?,
        @Part("up_m_price") upMPrice: RequestBody?,
        @Part("up_l_price") upLPrice: RequestBody?,
        @Part("is_topping") isTopping: RequestBody?,
        @Part("priority") priority: RequestBody?,
        @Part thumbnailImage: MultipartBody.Part?,
        @Part("categories_id[]") categoriesId: List<@JvmSuppressWildcards RequestBody>?,
        @Part("toppings_id[]") toppingsId: List<@JvmSuppressWildcards RequestBody>?,
        @Part productDetailImages: List<MultipartBody.Part>?
    ): Call<ProductOperationResponseDto>

    /**
     * Delete product
     * Example: /api/admin/products/delete/{id}
     */
    @DELETE("admin/products/delete/{id}")
    fun deleteAdminProduct(
        @Path("id") productId: String
    ): Call<SuccessResponse>

}

