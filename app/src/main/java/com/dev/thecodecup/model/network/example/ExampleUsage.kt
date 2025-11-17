package com.dev.thecodecup.model.network.example

/**
 * Example usage of the API integration in your Compose screens
 * 
 * This file demonstrates how to use ProductViewModel in your existing screens.
 * You can delete this file after understanding the integration.
 */

/*
// In your Composable screen (e.g., HomeScreen.kt):

import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HomeScreen(navController: NavController) {
    // Create ViewModel instance
    val productViewModel: ProductViewModel = viewModel()
    
    // Collect states
    val products by productViewModel.products.collectAsState()
    val categories by productViewModel.categories.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.error.collectAsState()
    
    // Load data when screen is first displayed
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
        productViewModel.loadCategories()
    }
    
    // Your UI
    Column {
        // Show loading indicator
        if (isLoading) {
            CircularProgressIndicator()
        }
        
        // Show error if any
        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = Color.Red
            )
        }
        
        // Display products
        LazyColumn {
            items(products) { product ->
                Text(text = product.productName)
                Text(text = "$${product.price}")
                // Use Coil to load images
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.productName
                )
            }
        }
    }
}

// Example: Filter products by category
Button(onClick = { 
    productViewModel.filterByCategory("category_id_here") 
}) {
    Text("Filter by Category")
}

// Example: Search products
TextField(
    value = searchText,
    onValueChange = { query ->
        productViewModel.searchProducts(query)
    },
    label = { Text("Search") }
)

// Example: Refresh data
Button(onClick = { productViewModel.refresh() }) {
    Text("Refresh")
}

// Example: Get specific product details
productViewModel.loadProductById("product_id_here") { product ->
    if (product != null) {
        // Navigate to detail screen or show dialog
        println("Product: ${product.productName}")
    }
}

*/

/**
 * Example: Setting up authentication token after login
 */
/*
// In your login success handler:
import com.dev.thecodecup.model.network.NetworkModule

// After successful login, set the token
val token = "your_jwt_token_from_login_response"
NetworkModule.tokenProvider = { token }

// Or if you're storing token in SharedPreferences/DataStore:
NetworkModule.tokenProvider = { 
    sharedPreferences.getString("auth_token", null)
}
*/

/**
 * Example: Manual API call without ViewModel
 */
/*
import com.dev.thecodecup.model.network.repository.ProductRemoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun manualApiCall() {
    val repository = ProductRemoteRepository.getInstance()
    
    CoroutineScope(Dispatchers.Main).launch {
        repository.getAllProducts(
            limit = 10,
            searchText = "coffee",
            categoryId = "all"
        ).onSuccess { products ->
            println("Got ${products.size} products")
            products.forEach { product ->
                println("${product.productName} - $${product.price}")
            }
        }.onFailure { exception ->
            println("Error: ${exception.message}")
        }
    }
}
*/

