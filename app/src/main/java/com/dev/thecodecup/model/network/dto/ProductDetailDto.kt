package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response for GET /api/admin/products/{id}
 */
@JsonClass(generateAdapter = true)
data class ProductDetailResponseDto(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "data")
    val data: ProductDetailDataDto
)

@JsonClass(generateAdapter = true)
data class ProductDetailDataDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "status")
    val status: String,  // "active" or "inactive"

    @Json(name = "is_topping")
    val isTopping: Int,

    @Json(name = "price")
    val price: String,

    @Json(name = "cost")
    val cost: String,

    @Json(name = "up_m_price")
    val upMPrice: String,

    @Json(name = "up_l_price")
    val upLPrice: String,

    @Json(name = "priority")
    val priority: Int,

    @Json(name = "categories_id")
    val categoriesId: List<String>,

    @Json(name = "toppings_id")
    val toppingsId: List<ToppingWithPrice>? = null,

    @Json(name = "thumbnailImage")
    val thumbnailImage: String? = null,

    @Json(name = "productDetailImages")
    val productDetailImages: List<ProductImageDto>? = null
)

@JsonClass(generateAdapter = true)
data class ToppingWithPrice(
    @Json(name = "topping_id")
    val toppingId: String,

    @Json(name = "extra_price")
    val extraPrice: String
)

@JsonClass(generateAdapter = true)
data class ProductImageDto(
    @Json(name = "id")
    val id: Int,

    @Json(name = "image_url")
    val imageUrl: String
)

/**
 * Response for POST /api/admin/products/add and POST /api/admin/products/update/{id}
 */
@JsonClass(generateAdapter = true)
data class ProductOperationResponseDto(
    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: ProductDataDto? = null
)

@JsonClass(generateAdapter = true)
data class ProductDataDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "status")
    val status: String,

    @Json(name = "price")
    val price: String,

    @Json(name = "cost")
    val cost: String,

    @Json(name = "up_m_price")
    val upMPrice: String,

    @Json(name = "up_l_price")
    val upLPrice: String,

    @Json(name = "is_topping")
    val isTopping: String,

    @Json(name = "priority")
    val priority: String
)
