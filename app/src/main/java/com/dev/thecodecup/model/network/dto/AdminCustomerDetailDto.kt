package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json

data class AdminCustomerDetailDto(
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "full_name")
    val fullName: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "phone_number")
    val phoneNumber: String? = null,

    @Json(name = "date_registered")
    val dateRegistered: String? = null,

    @Json(name = "date_of_birth")
    val dateOfBirth: String? = null,

    @Json(name = "gender")
    val gender: String? = null,

    @Json(name = "province")
    val province: String? = null,

    @Json(name = "district")
    val district: String? = null,

    @Json(name = "ward")
    val ward: String? = null,

    @Json(name = "street")
    val street: String? = null,

    @Json(name = "customer_number")
    val customerNumber: String? = null,

    @Json(name = "orders")
    val orders: List<AdminCustomerOrderDto>? = null
)

data class AdminCustomerDetailResponseDto(
    @Json(name = "data")
    val data: AdminCustomerDetailDto? = null
)
