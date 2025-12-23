package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json

data class AdminCustomerOrderDto(
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "order_number")
    val orderNumber: String? = null,

    @Json(name = "order_status")
    val orderStatus: String? = null,

    @Json(name = "order_total")
    val orderTotal: String? = null,

    @Json(name = "payment_method")
    val paymentMethod: String? = null,

    @Json(name = "payment_status")
    val paymentStatus: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "feedback")
    val feedback: String? = null,

    @Json(name = "rating")
    val rating: Int? = null
)
