package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdminOrderDetailResponseDto(
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "data")
    val data: AdminOrderDetailDto? = null
)

@JsonClass(generateAdapter = true)
data class AdminOrderDetailDto(
    @Json(name = "type") val type: String? = null,
    @Json(name = "order_number") val orderNumber: String? = null,
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "date_created") val dateCreated: String? = null,
    @Json(name = "host_id") val hostId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "order_total") val orderTotal: String? = null,
    @Json(name = "count_product") val countProduct: Int? = null,
    @Json(name = "order_detail") val orderDetail: List<AdminOrderItemDto>? = null,
    @Json(name = "customer_info") val customerInfo: AdminCustomerInfoDto? = null,
    @Json(name = "shipping_info") val shippingInfo: AdminShippingInfoDto? = null,
    @Json(name = "payment_info") val paymentInfo: AdminPaymentInfoDto? = null,
    @Json(name = "discount") val discount: Int? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "feedback") val feedback: AdminFeedbackDto? = null,
    @Json(name = "vouchers") val vouchers: List<AdminVoucherDto>? = null,
    @Json(name = "creator_info") val creatorInfo: AdminCreatorInfoDto? = null,
    @Json(name = "status_history") val statusHistory: List<AdminStatusHistoryDto>? = null,
    @Json(name = "total_price") val totalPrice: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminOrderItemDto(
    @Json(name = "order_detail_number") val orderDetailNumber: String? = null,
    @Json(name = "product_id") val productId: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_price") val productPrice: String? = null,
    @Json(name = "size") val size: String? = null,
    @Json(name = "quantity") val quantity: Int? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "total_price") val totalPrice: String? = null,
    @Json(name = "count_topping") val countTopping: Int? = null,
    @Json(name = "toppings") val toppings: List<AdminOrderToppingDto>? = null
)

@JsonClass(generateAdapter = true)
data class AdminOrderToppingDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "topping_id") val toppingId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "price") val price: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminCustomerInfoDto(
    @Json(name = "customer_id") val customerId: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    @Json(name = "customer_email") val customerEmail: String? = null,
    @Json(name = "customer_level") val customerLevel: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminShippingInfoDto(
    @Json(name = "from_name") val fromName: String? = null,
    @Json(name = "from_address") val fromAddress: String? = null,
    @Json(name = "to_name") val toName: String? = null,
    @Json(name = "to_address") val toAddress: String? = null,
    @Json(name = "receiver_phone") val receiverPhone: String? = null,
    @Json(name = "province") val province: String? = null,
    @Json(name = "district") val district: String? = null,
    @Json(name = "ward") val ward: String? = null,
    @Json(name = "street") val street: String? = null,
    @Json(name = "shipping_fee") val shippingFee: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminPaymentInfoDto(
    @Json(name = "payment_method") val paymentMethod: String? = null,
    @Json(name = "payment_status") val paymentStatus: String? = null,
    @Json(name = "payment_link") val paymentLink: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminFeedbackDto(
    @Json(name = "rating") val rating: Int? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "feedback_time") val feedbackTime: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminVoucherDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminCreatorInfoDto(
    @Json(name = "creator_id") val creatorId: String? = null,
    @Json(name = "creator_name") val creatorName: String? = null,
    @Json(name = "creator_email") val creatorEmail: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminStatusHistoryDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "changed_at") val changedAt: String? = null,
    @Json(name = "changed_by") val changedBy: AdminChangedByDto? = null,
    @Json(name = "note") val note: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminChangedByDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "email") val email: String? = null
)
