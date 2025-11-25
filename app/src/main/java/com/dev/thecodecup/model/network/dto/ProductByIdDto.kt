package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ToppingDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "price") val price: Double?,
    @Json(name = "is_selected") val isSelected: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SizeDto(
    @Json(name = "name") val name: String?,
    @Json(name = "price") val price: Double?
)

@JsonClass(generateAdapter = true)
data class ProductDetailImageDto(
    @Json(name = "id") val id: Int,
    @Json(name = "image_url") val imageUrl: String?
)

@JsonClass(generateAdapter = true)
data class ProductByIdDto(

    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String?,

    @Json(name = "price")
    val price: String,

    @Json(name = "image_url")
    val imageUrl: String?,

    @Json(name = "topping_list")
    val toppingList: List<ToppingDto>? = emptyList(),

    @Json(name = "productDetailImages")
    val productDetailImages: List<ProductDetailImageDto>? = emptyList(),

    @Json(name = "size_list")
    val sizeList: List<SizeDto>? = emptyList()
)
