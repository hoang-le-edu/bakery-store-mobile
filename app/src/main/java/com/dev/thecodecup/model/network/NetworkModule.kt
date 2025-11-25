package com.dev.thecodecup.model.network

import android.util.Log
import com.dev.thecodecup.BuildConfig
import com.dev.thecodecup.model.auth.AuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    /**
     * Token provider - set this after user login or from stored session
     * Example: NetworkModule.tokenProvider = { "your_jwt_token" }
     */
    @Volatile
    var tokenProvider: (() -> String?)? = null

    /**
     * Auth interceptor - adds Authorization header if token is available
     */
    private val authInterceptor = Interceptor { chain ->
//        val originalRequest = chain.request()
//        val requestBuilder = originalRequest.newBuilder()
//
//        // Add token if available
//        tokenProvider?.invoke()?.takeIf { it.isNotBlank() }?.let { token ->
//            requestBuilder.addHeader("Authorization", "Bearer $token")
//        }
//
//        // Add common headers
//        requestBuilder
//            .addHeader("Accept", "application/json")
//            .addHeader("Content-Type", "application/json")
//
//        chain.proceed(requestBuilder.build())
        val original: Request = chain.request()
        val builder = original.newBuilder()

        val token = try { tokenProvider?.invoke() ?: AuthManager.getValidIdTokenBlocking() }
        catch (_: Throwable) { null }

        if (!token.isNullOrBlank()) {
            builder.addHeader("Authorization", "Bearer $token")
            if (BuildConfig.DEBUG) Log.d("AuthInt", "Authorization attached")
        } else if (BuildConfig.DEBUG) {
            Log.d("AuthInt", "No token -> skip Authorization")
        }

        // Header chung
        builder.addHeader("Accept", "application/json")
        // KHÔNG ép Content-Type cho GET; chỉ nên set khi có body

        var res: Response = chain.proceed(builder.build())

        // 2) Nếu 401 -> thử refresh (AuthManager.getValidIdTokenBlocking()) rồi gọi lại 1 lần
        if (res.code == 401) {
            res.close()
            val newToken = try { tokenProvider?.invoke() ?: AuthManager.getValidIdTokenBlocking() }
            catch (_: Throwable) { null }
            if (!newToken.isNullOrBlank()) {
                if (BuildConfig.DEBUG) Log.d("AuthInt", "Retry with refreshed token")
                val retryReq = original.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newToken")
                    .addHeader("Accept", "application/json")
                    .build()
                res = chain.proceed(retryReq)
            }
        }
        res
    }
    
    /**
     * Logging interceptor - logs HTTP requests and responses in debug mode
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    /**
     * OkHttp client with interceptors and timeout configuration
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * Moshi instance for JSON parsing
     */
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    /**
     * API service instance (original products API)
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    /**
     * Bakery API service instance (cart, checkout, orders)
     * Used for: ProductDetail (with sizes/toppings), Cart, Checkout, Orders
     */
    val bakeryApiService: com.dev.thecodecup.model.network.api.BakeryApiService by lazy {
        retrofit.create(com.dev.thecodecup.model.network.api.BakeryApiService::class.java)
    }
}

