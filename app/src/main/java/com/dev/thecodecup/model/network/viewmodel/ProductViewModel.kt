package com.dev.thecodecup.model.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dev.thecodecup.model.network.dto.CategoryDto
import com.dev.thecodecup.model.network.dto.ProductDto
import com.dev.thecodecup.model.network.repository.CategoryRemoteRepository
import com.dev.thecodecup.model.network.repository.ProductRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    
    private val productRepository = ProductRemoteRepository.getInstance()
    private val categoryRepository = CategoryRemoteRepository.getInstance()
    
    // Products state
    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()
    
    // Categories state
    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Selected category
    private val _selectedCategory = MutableStateFlow<String?>("all")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * Load all products with optional filters
     */
    fun loadProducts(
        limit: Int? = null,
        searchText: String? = null,
        categoryId: String? = "all"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            productRepository.getAllProducts(limit, searchText, categoryId)
                .onSuccess { productList ->
                    _products.value = productList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load products"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Load product by ID
     */
    fun loadProductById(productId: String, onResult: (ProductDto?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    onResult(product)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load product"
                    onResult(null)
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Search products
     */
    fun searchProducts(query: String, limit: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _searchQuery.value = query
            
            if (query.isBlank()) {
                loadProducts()
            } else {
                productRepository.searchProducts(query, limit)
                    .onSuccess { productList ->
                        _products.value = productList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Search failed"
                    }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Load all categories
     */
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            categoryRepository.getAllCategories()
                .onSuccess { categoryList ->
                    _categories.value = categoryList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load categories"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Filter products by category
     */
    fun filterByCategory(categoryId: String) {
        _selectedCategory.value = categoryId
        loadProducts(categoryId = categoryId, searchText = _searchQuery.value.ifBlank { null })
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        loadProducts(
            searchText = _searchQuery.value.ifBlank { null },
            categoryId = _selectedCategory.value
        )
        loadCategories()
    }
}

