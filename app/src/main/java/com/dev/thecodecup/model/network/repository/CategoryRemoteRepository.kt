package com.dev.thecodecup.model.network.repository

import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRemoteRepository {
    
    private val apiService = NetworkModule.bakeryApiService
    
    /**
     * Get all categories
     * @return Result containing list of CategoryDto or error
     */
    suspend fun getAllCategories(): Result<List<CategoryWithProductsDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllCategories()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // Convert CategoryDto list to CategoryWithProductsDto list
                    val categories = body.data?.map { category ->
                        CategoryWithProductsDto(
                            categoryId = category.categoryId,
                            categoryName = category.categoryName,
                            categoryPriority = null,
                            categoryDescription = category.description,
                            productList = emptyList() // Will be populated by products API
                        )
                    } ?: emptyList()
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Empty response body or success=false"))
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

