package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Order model - matches API response (tạm thời)
 * Gợi ý API fields:
 *  - order_id         : String
 *  - receiver_name    : String
 *  - order_status     : String  (PENDING / ON_GOING / CANCELLED / COMPLETED ...)
 *  - order_time       : String
 *  - order_total      : String  (giống product_price, server thường trả về String)
 */
@JsonClass(generateAdapter = true)
data class OrderDto(
    @Json(name = "order_id")
    val orderId: String,

    @Json(name = "receiver_name")
    val receiverName: String,

    @Json(name = "order_status")
    val orderStatus: String,

    @Json(name = "order_time")
    val orderTime: String,

    @Json(name = "order_total")
    val orderTotal: String
) {
    // Helper: total dưới dạng Double
    val total: Double
        get() = orderTotal.toDoubleOrNull() ?: 0.0
}

