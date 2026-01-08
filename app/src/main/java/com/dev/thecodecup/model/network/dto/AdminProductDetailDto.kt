package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdminToppingPriceDto(
    @Json(name = "topping_id") val toppingId: String?,
    @Json(name = "topping_name") val toppingName: String? = null,
    @Json(name = "extra_price") val extraPrice: String?
)

@JsonClass(generateAdapter = true)
data class AdminProductDetailDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "is_topping") val toppingFlag: Int?,
    @Json(name = "price") val price: String?,
    @Json(name = "cost") val cost: String?,
    @Json(name = "up_m_price") val upMPrice: String?,
    @Json(name = "up_l_price") val upLPrice: String?,
    @Json(name = "priority") val priority: Int?,
    @Json(name = "categories_id") val categoriesId: List<String> = emptyList(),
    @Json(name = "categories_name") val categoriesName: List<String>? = null,
    @Json(name = "toppings_id") val toppingsId: List<AdminToppingPriceDto> = emptyList(),
    @Json(name = "thumbnailImage") val thumbnailImage: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "productDetailImages") val productDetailImages: List<ProductDetailImageDto> = emptyList()
)
