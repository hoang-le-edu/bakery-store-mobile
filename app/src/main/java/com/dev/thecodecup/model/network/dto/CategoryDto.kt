package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "id")
    val categoryId: String,
    
    @Json(name = "name")
    val categoryName: String,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "type")
    val type: String? = null,
    
    @Json(name = "priority")
    val priority: Int? = null,
    
    @Json(name = "image_path")
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

