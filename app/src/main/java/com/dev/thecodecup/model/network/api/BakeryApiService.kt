package com.dev.thecodecup.model.network.api

import com.dev.thecodecup.model.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Bakery Store API Service
 * Base URL: https://bepmetay.id.vn/api
 * 
 * All endpoints require Firebase Auth token in header:
 * Authorization: Bearer {firebaseIdToken}
 */
interface BakeryApiService {
    
    // ==================== Category APIs ====================
    
    @GET("categories/options/all")
    suspend fun getAllCategories(): Response<CategoriesResponse>
    
    // ==================== Product APIs ====================
    
    /**
     * Get all products with categories and filters
     * GET /api/customer/products/all?limit=&searchText=&category_id=
     */
    @GET("customer/products/all")
    suspend fun getAllProducts(
        @Query("limit") limit: Int? = null,
        @Query("searchText") searchText: String? = null,
        @Query("category_id") categoryId: String? = "all"
    ): Response<ProductsResponse>
    
    /**
     * Get detailed product information including sizes and toppings
     * Used in: ProductDetailScreen
     */
    @GET("customer/product/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: String
    ): Response<ProductDetailResponse>
    
    // ==================== Cart APIs ====================

    /**
     * Create a new empty cart
     * Used in: CartScreen, ProductDetailScreen
     */
    @POST("cart/createCart")
    suspend fun createCart(@Body request: CreateCartRequest): Response<SingleCartResponse>
    
    /**
     * Add product to cart (creates new cart or adds to existing)
     * Used in: ProductDetailScreen -> Add to Cart button
     */
    @POST("cart/addProductToCart")
    suspend fun addProductToCart(
        @Body request: AddToCartRequest
    ): Response<SuccessResponse>
    
    /**
     * Fetch all user's carts (multiple draft orders)
     * Used in: CartScreen on open
     */
    @GET("cart/fetchCart")
    suspend fun fetchCart(): Response<CartResponse>
    
    /**
     * Update existing product in cart
     * Used in: CartScreen -> Edit product
     */
    @PUT("cart/updateProductInCart")
    suspend fun updateProductInCart(
        @Body request: UpdateCartProductRequest
    ): Response<SuccessResponse>

    @POST("cart/removeProductFromCart")
    suspend fun removeProductFromCart(
        @Body request: RemoveProductFromCartRequest
    ): Response<SuccessResponse>
    
    /**
     * Delete entire cart (draft order)
     * NOTE: Changed from @DELETE to @PUT to match server implementation.
     */
    @PUT("cart/deleteCart")
    suspend fun deleteCart(
        @Query("order_id") orderId: String
    ): Response<SuccessResponse>
    
    @DELETE("cart/deleteToppingInCart")
    suspend fun deleteToppingFromCart(
        @Query("order_id") orderId: String,
        @Query("order_detail_id") orderDetailId: String,
        @Query("topping_id") toppingId: String
    ): Response<SuccessResponse>
    
    // ==================== Order APIs ====================
    
    /**
     * Proceed with checkout (convert draft cart to real order)
     * Used in: CheckoutScreen -> Place Order button
     */
    @POST("orders/proceed")
    suspend fun proceedOrder(
        @Body request: CheckoutRequest
    ): Response<SuccessResponse>
    
    /**
     * Load customer's orders grouped by status
     * Used in: OrderScreen tabs (Wait For Approval, In Progress, Completed, Cancelled)
     */
    @GET("loadCustomerOrders")
    suspend fun loadCustomerOrders(): Response<CustomerOrdersResponse>
    
    /**
     * Cancel order (only if status == "Wait For Approval")
     * Used in: OrderDetailScreen -> Cancel button
     */
    @POST("orders/cancel")
    suspend fun cancelOrder(
        @Body request: CancelOrderRequest
    ): Response<SuccessResponse>
    
    // ==================== Payment APIs ====================
    
    /**
     * Create payment link for online banking
     * Used in: CheckoutScreen -> Pay Now button (if payment_method == "Banking")
     */
    @POST("payment/createLink")
    suspend fun createPaymentLink(
        @Body request: CreatePaymentLinkRequest
    ): Response<PaymentLinkResponse>
    
    // ==================== Admin APIs (require premiumAccount=true in JWT) ====================
    
    /**
     * Load all products grouped by category (Admin only)
     * Used in: EmployeeScreens -> ProductManagementScreen
     */
    @GET("admin/products/all")
    suspend fun loadAllProducts(): Response<AdminProductsResponse>
    
    /**
     * Load all orders across all users (Admin only)
     * Used in: EmployeeScreens -> OrderManagementScreen
     */
    @GET("admin/orders/all")
    suspend fun loadAllOrders(): Response<AdminOrdersResponse>
    
    /**
     * Update order status (Admin only)
     * Used in: OrderManagementScreen -> Status dropdown
     */
    @PUT("admin/orders/updateStatus")
    suspend fun updateOrderStatus(
        @Body request: UpdateOrderStatusRequest
    ): Response<SuccessResponse>
}

// ==================== Request DTOs ====================

data class CreateCartRequest(
    val type: String,
    val custom_name: String? = null
)

data class AddToCartRequest(
    val product: CartProductRequest,
    val order_ids: List<String> = emptyList()  // Empty = new cart, Filled = add to existing cart
)

data class RemoveProductFromCartRequest(
    val cart_id: String,
    val order_detail_id: String
)

data class CartProductRequest(
    val product_id: String,
    val total_price: Int,
    val size: String = "",  // Default to empty if not provided
    val toppings_id: List<String> = emptyList(),  // Default to empty list
    val note: String = "",  // Default to empty
    val quantity: Int
)

data class UpdateCartProductRequest(
    val order_id: String,
    val order_detail_id: String,
    val size: String,
    val toppings_id: List<String>,
    val quantity: Int,
    val note: String,
    val total_price: Int
)

data class CheckoutRequest(
    val order_id: String,
    val receiver_name: String,
    val receiver_address: String,  // Full address string
    val payment_method: String,  // "Cash" or "Banking"
    val voucher: String = "",
    val voucher_shipping: String = "",
    val note: String = "",
    val province: String,  // ProvinceID from tinh_tp.json
    val district: String,  // DistrictID from quan_huyen.json
    val ward: String,  // WardCode from xa_phuong.json
    val street: String,
    val phone_number: String,
    val shipping_fee: Int,
    val discount_number: Int,
    val order_total: Int
)

data class CancelOrderRequest(
    val order_id: String,
    val reason: String = ""
)

data class CreatePaymentLinkRequest(
    val order_id: String
)

data class UpdateOrderStatusRequest(
    val order_id: String,
    val status: String  // "Wait For Approval", "In Progress", "Completed", "Cancelled"
)

// ==================== Response DTOs ====================

data class SingleCartResponse(
    val message: String,
    val data: Cart
)

data class ProductDetailResponse(
    val success: Boolean,
    val data: ProductDetail
)

data class ProductDetail(
    val id: String,
    val name: String,
    val description: String?,
    val price: String,  // e.g. "6000.00"
    val image_url: String,
    val productDetailImages: List<ProductImage>,
    val size_list: List<Size>,
    val topping_list: List<Topping>
)

data class ProductImage(
    val image_url: String
)

data class Size(
    val name: String,  // "S", "M", "L"
    val price: Int  // Additional price for this size (0 for base size)
)

data class Topping(
    val id: String,
    val name: String,
    val price: String,  // e.g. "1000.00"
    var is_selected: Boolean = false  // Mutable for UI selection
)

data class CartResponse(
    val message: String,
    val data: List<Cart>,
    val userInfo: UserInfo? = null
)

data class Cart(
    val order_id: String,
    val order_number: String,
    val name: String,  // Display name for cart tab
    val date_created: String,  // ISO 8601 format
    val host_id: String,
    val count_product: Int,
    val total_price: Int,
    val order_detail: List<CartOrderDetail>
)

data class CartOrderDetail(
    val id: String,  // order_detail_id
    val order_detail_number: String,
    val product_id: String,
    val product_name: String,
    val product_price: String,
    val size: String,
    val quantity: Int,
    val image: String,
    val note: String,
    val total_price: String,
    val count_topping: Int,
    val toppings: List<CartTopping>
)

data class CartTopping(
    val id: String,  // topping_cart_uuid (for deletion)
    val topping_id: String,  // topping definition UUID
    val name: String,
    val price: String
)

data class UserInfo(
    val full_name: String,
    val phone_number: String
)

data class CustomerOrdersResponse(
    val message: String,
    val data: Map<String, List<Order>>  // Key = status ("Wait For Approval", "In Progress", etc.)
)

data class Order(
    val id: String,  // Same as order_id in some APIs
    val order_id: String?,
    val order_number: String,
    val date_created: String,
    val host_id: String,
    val payment_method: String,
    val payment_status: String,  // "pending", "paid"
    val receiver_name: String,
    val receiver_address: String?,
    val receiver_phone: String?,
    val order_status: String,  // "Wait For Approval", "In Progress", "Completed", "Cancelled", "Draft"
    val status: String?,  // Alternative field name
    val count_product: Int,
    val order_total: String?,
    val total_price: String?,  // Alternative field name
    val order_date: String,
    val rate: Int?,
    val feedback: String,
    val note: String?,
    val created_at: String?,
    val source: String?,  // "Online" or "Offline"
    val order_detail: List<OrderDetail>? = null
)

data class OrderDetail(
    val id: String,
    val order_detail_number: String,
    val product_id: String,
    val product_name: String,
    val product_price: String,
    val size: String,
    val quantity: Int,
    val image: String?,
    val note: String,
    val total_price: String,
    val count_topping: Int,
    val toppings: List<OrderTopping>? = null
)

data class OrderTopping(
    val id: String,
    val topping_id: String,
    val name: String,
    val price: String
)

data class PaymentLinkResponse(
    val success: Boolean,
    val data: PaymentLinkData
)

data class PaymentLinkData(
    val checkoutUrl: String,  // QR code payment URL
    val qrCodeUrl: String
)

data class AdminProductsResponse(
    val message: String,
    val data: List<ProductCategory>,
    val topping_data: List<ToppingCategory>,
    val products_count: Int,
    val pagination: Pagination
)

data class ProductCategory(
    val category_name: String,
    val category_id: String,
    val category_priority: Int?,
    val category_description: String?,
    val product_list: List<AdminProduct>
)

data class AdminProduct(
    val product_id: String,
    val product_name: String,
    val product_description: String?,
    val product_price: String
)

data class ToppingCategory(
    val category_name: String,
    val category_id: String,
    val topping_list: List<AdminTopping>
)

data class AdminTopping(
    val product_id: String,  // Toppings are products in admin view
    val product_name: String,
    val product_description: String?,
    val product_price: String
)

data class Pagination(
    val last_product_id: String,
    val first_product_id: String,
    val has_more: Boolean
)

data class AdminOrdersResponse(
    val message: String,
    val data: List<Order>  // Flat list of all orders
)

data class SuccessResponse(
    val success: Boolean? = null,
    val message: String
)
