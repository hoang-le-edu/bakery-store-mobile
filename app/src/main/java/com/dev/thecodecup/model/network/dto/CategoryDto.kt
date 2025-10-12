package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "category_id")
    val categoryId: String,
    
    @Json(name = "category_name")
    val categoryName: String,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "image_url")
    val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CategoriesResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "data")
    val data: List<CategoryDto>? = null,
    
    @Json(name = "message")
    val message: String? = null
)

