package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "data")
    val data: T? = null,
    
    @Json(name = "message")
    val message: String? = null,
    
    @Json(name = "error")
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "success")
    val success: Boolean = false,
    
    @Json(name = "message")
    val message: String? = null,
    
    @Json(name = "error")
    val error: String? = null
)

