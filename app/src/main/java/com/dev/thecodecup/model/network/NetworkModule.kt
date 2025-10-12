package com.dev.thecodecup.model.network

import com.dev.thecodecup.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        // Add token if available
        tokenProvider?.invoke()?.takeIf { it.isNotBlank() }?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        // Add common headers
        requestBuilder
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
        
        chain.proceed(requestBuilder.build())
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
     * API service instance
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

