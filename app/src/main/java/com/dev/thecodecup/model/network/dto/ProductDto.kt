package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Product model - matches actual API response
 * API returns: product_price as String, not Double
 */
@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "product_id")
    val productId: String,
    
    @Json(name = "product_name")
    val productName: String,
    
    @Json(name = "product_price")  // Changed from "price" to "product_price"
    val productPrice: String,  // Changed from Double to String (API returns String)
    
    @Json(name = "product_description")  // Changed from "description"
    val productDescription: String? = null,
    
    @Json(name = "product_image")  // Changed from "image_url"
    val productImage: String? = null
) {
    // Helper property to get price as Double
    val price: Double
        get() = productPrice.toDoubleOrNull() ?: 0.0
    
    // Helper property for backward compatibility
    val description: String?
        get() = productDescription
    
    val imageUrl: String?
        get() = productImage


}

/**
 * Category with nested product list
 * This matches the actual API structure where products are nested inside categories
 */
@JsonClass(generateAdapter = true)
data class CategoryWithProductsDto(
    @Json(name = "category_id")
    val categoryId: String,
    
    @Json(name = "category_name")
    val categoryName: String,
    
    @Json(name = "category_priority")
    val categoryPriority: Int? = null,
    
    @Json(name = "category_description")
    val categoryDescription: String? = null,
    
    @Json(name = "product_list")
    val productList: List<ProductDto> = emptyList()
)

/**
 * Main API response for products endpoint
 * Actual structure: data is array of categories, each containing product_list
 */
@JsonClass(generateAdapter = true)
data class ProductsResponse(
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: List<CategoryWithProductsDto>? = null,
    
    @Json(name = "products_count")
    val productsCount: Int? = null,
    
    @Json(name = "pagination")
    val pagination: PaginationDto? = null
) {
    // Helper to get flat list of all products from all categories
    fun getAllProducts(): List<ProductDto> {
        return data?.flatMap { it.productList } ?: emptyList()
    }
}

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @Json(name = "last_product_id")
    val lastProductId: String? = null,
    
    @Json(name = "has_more")
    val hasMore: Boolean = false
)

