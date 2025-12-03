package com.dev.thecodecup.model.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================
// Response của /api/auth/login
// =====================
@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "success")
    val success: Boolean? = null,

    @Json(name = "data")
    val data: LoginDataDto? = null,

    @Json(name = "message")
    val message: String? = null
)

// Phần "data" bên trong response
@JsonClass(generateAdapter = true)
data class LoginDataDto(
    @Json(name = "accessToken")
    val accessToken: String? = null,

    @Json(name = "refreshToken")
    val refreshToken: String? = null,

    @Json(name = "user")
    val user: UserDto? = null
)

// Thông tin user trả về từ API login
@JsonClass(generateAdapter = true)
data class UserDto(

    @Json(name = "id")
    val id: String? = null,

    @Json(name = "firebase_uid")
    val firebaseUid: String? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "phone_number")
    val phoneNumber: String? = null,

    @Json(name = "email_verified_at")
    val emailVerifiedAt: String? = null,

    @Json(name = "two_factor_secret")
    val twoFactorSecret: String? = null,

    @Json(name = "two_factor_recovery_codes")
    val twoFactorRecoveryCodes: String? = null,

    @Json(name = "is_admin")
    val isAdmin: Int? = null,

    // QUAN TRỌNG: dùng để phân biệt admin / customer
    @Json(name = "user_type")
    val userType: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null,

    @Json(name = "deleted_at")
    val deletedAt: String? = null,

    @Json(name = "api_token")
    val apiToken: String? = null,

    @Json(name = "created_by")
    val createdBy: String? = null,

    @Json(name = "custom_token")
    val customToken: String? = null
)
