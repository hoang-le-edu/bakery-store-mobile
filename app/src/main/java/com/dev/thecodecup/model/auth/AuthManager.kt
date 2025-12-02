// app/src/main/java/com/dev/thecodecup/model/auth/AuthManager.kt
package com.dev.thecodecup.model.auth

import android.content.Context
import android.content.SharedPreferences
import com.dev.thecodecup.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Json
import java.util.concurrent.TimeUnit

object AuthManager {
    private const val SP_NAME = "auth_prefs"
    private const val KEY_ID = "id_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_EXPIRES_AT = "expires_at" // epoch ms

    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    fun setTokens(idToken: String, refreshToken: String, expiresInSec: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresInSec - 30) * 1000 // trừ buffer 30s
        sp.edit()
            .putString(KEY_ID, idToken)
            .putString(KEY_REFRESH, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun clearTokens() {
        sp.edit().clear().apply()
    }

    fun getIdTokenOrNull(): String? = sp.getString(KEY_ID, null)
    private fun getRefreshTokenOrNull(): String? = sp.getString(KEY_REFRESH, null)
    private fun getExpiresAt(): Long = sp.getLong(KEY_EXPIRES_AT, 0L)

    fun isExpired(): Boolean = System.currentTimeMillis() >= getExpiresAt()

    fun isLoggedIn(): Boolean {
        val token = getIdTokenOrNull()
        if (token.isNullOrEmpty()) {
            return false
        }
        return !isExpired()
    }
    /** Lấy idToken hợp lệ; nếu hết hạn sẽ refresh (chặn tạm thời bằng runBlocking). */
    fun getValidIdTokenBlocking(): String? {
        val token = getIdTokenOrNull()
        if (token.isNullOrEmpty()) return null
        if (!isExpired()) return token
        return refreshBlocking()
    }

    // ===== Refresh với Secure Token API =====
    private interface SecureTokenApi {
        @FormUrlEncoded
        @POST("token?key=${BuildConfig.FIREBASE_WEB_API_KEY}")
        suspend fun refresh(
            @Field("grant_type") grantType: String = "refresh_token",
            @Field("refresh_token") refreshToken: String
        ): RefreshResp
    }

    private val secureRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://securetoken.googleapis.com/v1/")
            .client(OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val secureApi by lazy { secureRetrofit.create(SecureTokenApi::class.java) }

    data class RefreshResp(
        @Json(name = "id_token") val idToken: String,
        @Json(name = "refresh_token") val refreshToken: String,
        @Json(name = "expires_in") val expiresIn: String
    )

    private fun refreshBlocking(): String? = runBlocking {
        val r = getRefreshTokenOrNull() ?: return@runBlocking null
        return@runBlocking try {
            val resp = secureApi.refresh(refreshToken = r)
            val newId = resp.idToken
            val newRefresh = resp.refreshToken
            val exp = resp.expiresIn.toLongOrNull() ?: 3600L
            setTokens(newId, newRefresh, exp)
            newId
        } catch (_: Exception) {
            null
        }
    }
}
