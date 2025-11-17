package com.dev.thecodecup.model.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.dev.thecodecup.model.network.dto.CategoryDto
import com.dev.thecodecup.model.network.dto.ProductDto
import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto
import com.dev.thecodecup.model.network.dto.ProductByIdDto
import com.dev.thecodecup.model.network.repository.CategoryRemoteRepository
import com.dev.thecodecup.model.network.repository.ProductRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val _productsLiveData = MutableLiveData<List<ProductDto>>()
    val productsLiveData: LiveData<List<ProductDto>> = _productsLiveData

    private val _categoriesLiveData = MutableLiveData<List<CategoryWithProductsDto>>()
    val categoriesLiveData: LiveData<List<CategoryWithProductsDto>> = _categoriesLiveData


    private val productRepository = ProductRemoteRepository.getInstance()
    private val categoryRepository = CategoryRemoteRepository.getInstance()

    // Products state
    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    // Categories state
    private val _categories = MutableStateFlow<List<CategoryWithProductsDto>>(emptyList())
    val categories: StateFlow<List<CategoryWithProductsDto>> = _categories.asStateFlow()

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
                    android.util.Log.d("ProductVM", "SUCCESS. Kích thước nhận từ Repo: ${productList.size} - Post LiveData...")
                    // Cập nhật LiveData (Java Activity)
                    _productsLiveData.postValue(productList)
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
    fun loadProductById(productId: String, onResult: (ProductByIdDto?) -> Unit) {
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
                    _categoriesLiveData.postValue(categoryList)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load categories"
                    _categoriesLiveData.postValue(emptyList())
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

    fun observeProductsLiveData(): LiveData<List<ProductDto>> = productsLiveData
    fun observeCategoriesLiveData(): LiveData<List<CategoryWithProductsDto>> = categoriesLiveData

}

//class ProductViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val _productsLiveData = MutableLiveData<List<ProductDto>>()
//    val productsLiveData: LiveData<List<ProductDto>> = _productsLiveData
//
//    private val _categoriesLiveData = MutableLiveData<List<CategoryWithProductsDto>>()
//    val categoriesLiveData: LiveData<List<CategoryWithProductsDto>> = _categoriesLiveData
//
//
//    // *** KHÔNG CẦN REPOSITORY NẾU MOCK DATA TRỰC TIẾP Ở ĐÂY ***
//    // private val productRepository = ProductRemoteRepository.getInstance()
//    // private val categoryRepository = CategoryRemoteRepository.getInstance()
//
//    // Products state
//    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
//    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()
//
//    // Categories state
//    private val _categories = MutableStateFlow<List<CategoryWithProductsDto>>(emptyList())
//    val categories: StateFlow<List<CategoryWithProductsDto>> = _categories.asStateFlow()
//
//    // Loading state
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    // Error state
//    private val _error = MutableStateFlow<String?>(null)
//    val error: StateFlow<String?> = _error.asStateFlow()
//
//    // Selected category
//    private val _selectedCategory = MutableStateFlow<String?>("all")
//    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
//
//    // Search query
//    private val _searchQuery = MutableStateFlow("")
//    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
//
//    /**
//     * Load all products with optional filters
//     */
//    fun loadProducts(
//        limit: Int? = null,
//        searchText: String? = null,
//        categoryId: String? = "all"
//    ) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            //  THAY THẾ LOGIC GỌI API BẰNG MOCK DATA
//            val mockProducts = mockProductsByCategory(categoryId)
//                .filter { p ->
//                    // Mô phỏng logic tìm kiếm nếu có searchText
//                    searchText.isNullOrBlank() || p.productName.contains(searchText, ignoreCase = true)
//                }
//
//            _products.value = mockProducts
//            _productsLiveData.postValue(mockProducts) // Cập nhật cả LiveData cho Activity cũ
//
//            _isLoading.value = false
//        }
//    }
//
//    /**
//     * Load product by ID
//     */
//    fun loadProductById(productId: String, onResult: (ProductDto?) -> Unit) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            // THAY THẾ LOGIC GỌI API BẰNG MOCK DATA
//            val product = mockAllProducts().find { it.productId == productId }
//            onResult(product)
//
//            _isLoading.value = false
//        }
//    }
//
//    /**
//     * Search products
//     */
//    fun searchProducts(query: String, limit: Int? = null) {
//        _searchQuery.value = query
//        // Sử dụng lại loadProducts, nó sẽ tự động lọc theo searchText mới.
//        // Chỉ cần gọi filterByCategory vì nó gọi loadProducts với searchText và categoryId hiện tại.
//        filterByCategory(_selectedCategory.value ?: "all")
//    }
//
//
//    /**
//     * Load all categories
//     */
//    fun loadCategories() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            // THAY THẾ LOGIC GỌI API BẰNG MOCK DATA
//            val mockCategories = mockCategories()
//            _categories.value = mockCategories
//            _categoriesLiveData.postValue(mockCategories) // Cập nhật cả LiveData cho Activity cũ
//
//            _isLoading.value = false
//        }
//    }
//
//
//    // --- MOCK DATA FUNCTIONS ---
//
//    /** Hàm tiện ích để tạo nhanh ProductDto */
//    private fun createProduct(id: String, name: String, price: String, priority: Int, image: String): ProductDto {
//        return ProductDto(
//            productId = id,
//            productName = name,
//            productPrice = price,
//            productDescription = null, // Giữ null
//            productImage = image
//        )
//    }
//
//    /** Tạo danh sách các Category và Product tương ứng (dữ liệu mẫu) */
//    private fun mockCategories(): List<CategoryWithProductsDto> {
//        // Tạo Products cho Category 1: Rau câu mini
//        val miniJellies = listOf(
//            createProduct("944042e1-dc0d-11ef-9219-5811227f7f82", "Rau câu cà phê dứa mini 100gr", "6000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/hjz0wWyuVJhH4VrPhyySPIcKNSlOjbnUqS8zrsrl.png"),
//            createProduct("9440428e-dc0d-11ef-9219-5811227f7f82", "Rau câu cà phê mini 100gr", "6000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/TNvGmG9MPnw8WPnC5i9XE6cDilixDRiw6dtfbSuA.png"),
//            createProduct("94404241-dc0d-11ef-9219-5811227f7f82", "Rau câu dừa mini 100gr", "6000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/on8PdGcyqvzu5NkXj5poaDMPK8EF16ho2zm2p4bq.png"),
//            createProduct("cc38183d-a210-45fa-a224-c2daec89ea2a", "Rau câu lá dứa mini 100gr", "6000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/Has1TjDXtdjdZNN7YnNguCOf8wOcFWxuExSly2ii.png")
//        )
//
//        // Tạo Products cho Category 2: Rau câu hoa lá
//        val flowerJellies = listOf(
//            createProduct("9440438a-dc0d-11ef-9219-5811227f7f82", "Rau câu cà phê dứa hoa lá", "30000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/NQRaY3YSSeivPz9gSyqHyfpf81TDm0S9iQNhnMLO.png"),
//            createProduct("9440434d-dc0d-11ef-9219-5811227f7f82", "Rau câu cà phê hoa lá", "30000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/FYaQY81qKlJNxWm0sjonhBgG5aIOjdDXWOOiFaai.png"),
//            createProduct("94404318-dc0d-11ef-9219-5811227f7f82", "Rau câu dừa hoa lá", "30000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/nJFXtU6gPHcpOWwOrozu6ebJ2ZTfhMbjhwk8C38M.png"),
//            createProduct("9440438b-dc0d-11ef-9219-5811227f7f82", "Rau câu ngẫu nhiên hoa lá", "30000.00", 1, "https://bepmetay.id.vn/storage/build/assets/Product/IO4LgNAfLQ5UA1HjxSx6M0qN1NfSKS5QDnx7sIIK.png")
//        )
//
//        // Tạo Category 1
//        val category1 = CategoryWithProductsDto(
//            categoryId = "2a1dbe77-b549-11ef-8332-70a6cc37ddf9",
//            categoryName = "Rau câu mini",
//            categoryPriority = 1,
//            productList = miniJellies
//        )
//
//        // Tạo Category 2
//        val category2 = CategoryWithProductsDto(
//            categoryId = "9b577267-b54a-11ef-8332-70a6cc37ddf9",
//            categoryName = "Rau câu hoa lá",
//            categoryPriority = 1,
//            productList = flowerJellies
//        )
//
//        // Thêm một Category thứ 3 (để test)
//        val category3 = CategoryWithProductsDto(
//            categoryId = "mock-id-flan",
//            categoryName = "Bánh Flan",
//            categoryPriority = 2,
//            productList = listOf(
//                createProduct("flan-001", "Flan Truyền Thống", "15000.00", 1, "https://example.com/images/flan-traditional.png"),
//                createProduct("flan-002", "Flan Phô Mai", "18000.00", 2, "https://example.com/images/flan-cheese.png")
//            )
//        )
//
//        return listOf(category1, category2, category3)
//    }
//
//    /** Trả về danh sách Product của Category tương ứng, hoặc tất cả nếu categoryId = "all" */
//    private fun mockProductsByCategory(categoryId: String?): List<ProductDto> {
//        val categories = mockCategories()
//
//        return when (categoryId) {
//            "all", null -> categories.flatMap { it.productList } // Trả về TẤT CẢ sản phẩm
//            else -> categories.find { it.categoryId == categoryId }?.productList ?: emptyList()
//        }
//    }
//
//    /** Lấy một danh sách phẳng của tất cả sản phẩm */
//    private fun mockAllProducts(): List<ProductDto> {
//        return mockCategories().flatMap { it.productList }
//    }
//
//        fun filterByCategory(categoryId: String) {
//        _selectedCategory.value = categoryId
//        loadProducts(categoryId = categoryId, searchText = _searchQuery.value.ifBlank { null })
//    }
//
//        /**
//     * Refresh all data
//     */
//    fun refresh() {
//        loadProducts(
//            searchText = _searchQuery.value.ifBlank { null },
//            categoryId = _selectedCategory.value
//        )
//        loadCategories()
//    }
//}

