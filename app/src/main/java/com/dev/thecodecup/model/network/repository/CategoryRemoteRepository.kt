package com.dev.thecodecup.model.network.repository

import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.model.network.dto.CategoryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRemoteRepository {
    
    private val apiService = NetworkModule.apiService
    
    /**
     * Get all categories
     * @return Result containing list of categories or error
     */
    suspend fun getAllCategories(): Result<List<CategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllCategories()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.data.orEmpty())
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to load categories"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        @Volatile
        private var instance: CategoryRemoteRepository? = null
        
        fun getInstance(): CategoryRemoteRepository =
            instance ?: synchronized(this) {
                instance ?: CategoryRemoteRepository().also { instance = it }
            }
    }
}

