package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response cho API: GET /api/admin/products/all
 */
@JsonClass(generateAdapter = true)
data class AdminProductsResponseDto(
    @Json(name = "message")
    val message: String? = null,

    @Json(name = "data")
    val data: List<AdminProductCategoryDto>? = null,

    @Json(name = "topping_data")
    val toppingData: List<AdminToppingCategoryDto>? = null
)

@JsonClass(generateAdapter = true)
data class AdminToppingCategoryDto(
    @Json(name = "category_name")
    val categoryName: String? = null,

    @Json(name = "category_id")
    val categoryId: String? = null,

    @Json(name = "topping_list")
    val toppingList: List<AdminProductDto>? = null
)

/**
 * Một category trong "data"
 */
@JsonClass(generateAdapter = true)
data class AdminProductCategoryDto(

    @Json(name = "category_name")
    val categoryName: String? = null,

    @Json(name = "category_id")
    val categoryId: String? = null,

    @Json(name = "category_priority")
    val categoryPriority: Int? = null,

    @Json(name = "category_description")
    val categoryDescription: String? = null,

    @Json(name = "product_list")
    val productList: List<AdminProductDto>? = null
)

/**
 * Một product trong "product_list"
 */
@JsonClass(generateAdapter = true)
data class AdminProductDto(

    @Json(name = "product_id")
    val productId: String? = null,

    @Json(name = "product_name")
    val productName: String? = null,

    @Json(name = "product_description")
    val productDescription: String? = null,

    @Json(name = "product_price")
    val productPrice: String? = null,

    @Json(name = "avg_rating")
    val avgRating: Int? = null,

    @Json(name = "review_count")
    val reviewCount: Int? = null,

    @Json(name = "product_image_url")
    val productImageUrl: String? = null
)
