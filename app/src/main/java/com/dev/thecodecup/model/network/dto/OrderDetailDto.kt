package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response for Order Detail API
 * Endpoint: GET /api/loadOrderDetail/{order_id}
 */
@JsonClass(generateAdapter = true)
data class OrderDetailResponse(
    @Json(name = "message")
    val message: String? = null,

    @Json(name = "data")
    val data: List<OrderDetailData>? = null
)

@JsonClass(generateAdapter = true)
data class OrderDetailData(
    @Json(name = "type")
    val type: String? = null,

    @Json(name = "order_number")
    val orderNumber: String? = null,

    @Json(name = "order_id")
    val orderId: String? = null,

    @Json(name = "date_created")
    val dateCreated: String? = null,

    @Json(name = "host_id")
    val hostId: String? = null,

    @Json(name = "status")
    val status: String? = null,

    @Json(name = "order_total")
    val orderTotal: Int? = null,

    @Json(name = "count_product")
    val countProduct: Int? = null,

    @Json(name = "order_detail")
    val orderDetail: List<OrderDetailItem>? = null,

    @Json(name = "customer_name")
    val customerName: String? = null,

    @Json(name = "customer_phone")
    val customerPhone: String? = null,

    @Json(name = "customer_level")
    val customerLevel: String? = null,

    @Json(name = "to_name")
    val toName: String? = null,

    @Json(name = "to_address")
    val toAddress: String? = null,

    @Json(name = "shipping_fee")
    val shippingFee: String? = null,

    @Json(name = "discount")
    val discount: Int? = null,

    @Json(name = "payment_method")
    val paymentMethod: String? = null,

    @Json(name = "feedback")
    val feedback: OrderFeedback? = null,

    @Json(name = "vouchers")
    val vouchers: List<OrderVoucher>? = null,

    @Json(name = "total_price")
    val totalPrice: Int? = null
)

@JsonClass(generateAdapter = true)
data class OrderDetailItem(
    @Json(name = "order_detail_number")
    val orderDetailNumber: String? = null,

    @Json(name = "product_id")
    val productId: String? = null,

    @Json(name = "product_name")
    val productName: String? = null,

    @Json(name = "product_price")
    val productPrice: String? = null,

    @Json(name = "size")
    val size: String? = null,

    @Json(name = "quantity")
    val quantity: Int? = null,

    @Json(name = "image")
    val image: String? = null,

    @Json(name = "note")
    val note: String? = null,

    @Json(name = "total_price")
    val totalPrice: String? = null,

    @Json(name = "count_topping")
    val countTopping: Int? = null,

    @Json(name = "toppings")
    val toppings: List<OrderDetailTopping>? = null
)

@JsonClass(generateAdapter = true)
data class OrderDetailTopping(
    @Json(name = "topping_id")
    val toppingId: String? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "price")
    val price: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderFeedback(
    @Json(name = "rating")
    val rating: Int? = null,

    @Json(name = "content")
    val content: String? = null,

    @Json(name = "feedback_time")
    val feedbackTime: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderVoucher(
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "voucher_code")
    val voucherCode: String? = null,

    @Json(name = "discount_amount")
    val discountAmount: String? = null,

    @Json(name = "discount_percent")
    val discountPercent: String? = null,

    @Json(name = "discount_type")
    val discountType: String? = null,

    @Json(name = "apply_type")
    val applyType: String? = null
)
