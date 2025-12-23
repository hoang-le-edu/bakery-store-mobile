package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response cho API: GET /api/admin/orders/all
 */
@JsonClass(generateAdapter = true)
data class AdminOrdersResponseDto(
    @Json(name = "message")
    val message: String? = null,

    @Json(name = "data")
    val data: List<AdminOrderDto>? = null
)

/**
 * Để thống kê cơ bản, chỉ cần một số field chính.
 * Sau này cần thêm gì thì khai báo thêm.
 */
@JsonClass(generateAdapter = true)
data class AdminOrderDto(

    @Json(name = "id")
    val id: String? = null,

    @Json(name = "order_id")
    val orderId: String? = null,

    @Json(name = "order_number")
    val orderNumber: String? = null,

    @Json(name = "receiver_name")
    val receiverName: String? = null,

    @Json(name = "receiver_address")
    val receiverAddress: String? = null,

    @Json(name = "order_total")
    val orderTotal: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "order_status")
    val orderStatus: String? = null,

    @Json(name = "payment_status")
    val paymentStatus: String? = null,

    @Json(name = "payment_method")
    val paymentMethod: String? = null,

    @Json(name = "customer_info")
    val customerInfo: CustomerInfoDto? = null
) {
    val customerName: String? get() = customerInfo?.customerName
}

@JsonClass(generateAdapter = true)
data class CustomerInfoDto(
    @Json(name = "customer_name")
    val customerName: String? = null,

    @Json(name = "customer_id")
    val customerId: String? = null,

    @Json(name = "customer_phone")
    val customerPhone: String? = null,

    @Json(name = "customer_email")
    val customerEmail: String? = null,

    @Json(name = "customer_level")
    val customerLevel: String? = null
)
