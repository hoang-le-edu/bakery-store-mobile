package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel

/**
 * Test screen để verify API integration
 * 
 * Màn hình này hiển thị:
 * - Loading state
 * - Error handling
 * - Products từ API
 * - Categories từ API
 * - Search functionality
 * - Category filter
 * 
 * Để test: Navigate đến screen này từ app của bạn
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen() {
    val productViewModel: ProductViewModel = viewModel()
    
    // Collect states
    val products by productViewModel.products.collectAsState()
    val categories by productViewModel.categories.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.error.collectAsState()
    val selectedCategory by productViewModel.selectedCategory.collectAsState()
    
    // Local state for search
    var searchQuery by remember { mutableStateOf("") }
    
    // Load data when screen is first displayed
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
        productViewModel.loadCategories()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Test Screen") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        error != null -> MaterialTheme.colorScheme.errorContainer
                        isLoading -> MaterialTheme.colorScheme.secondaryContainer
                        products.isNotEmpty() -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "API Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when {
                        error != null -> {
                            Text(
                                text = "❌ Error: $error",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        isLoading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⏳ Loading...")
                            }
                        }
                        products.isNotEmpty() -> {
                            Text(
                                text = "✅ Connected! Found ${products.size} products",
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        else -> {
                            Text("⚪ No data yet")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    productViewModel.searchProducts(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search products...") },
                placeholder = { Text("Try: coffee, cake, etc.") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categories (horizontal scroll)
            if (categories.isNotEmpty()) {
                Text(
                    text = "Categories (${categories.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { productViewModel.filterByCategory("all") },
                        label = { Text("All") }
                    )
                    
                    categories.take(5).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category.categoryId,
                            onClick = { productViewModel.filterByCategory(category.categoryId) },
                            label = { Text(category.categoryName) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Products List
            Text(
                text = "Products (${products.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Refresh Button
            Button(
                onClick = { productViewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Refresh Data")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Products LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (products.isEmpty() && !isLoading) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📭",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Text("No products found")
                                Text(
                                    text = "Try searching or refreshing",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                items(products) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Product Image
                            product.imageUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = product.productName,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
                            // Product Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.productName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = "$${"%.2f".format(product.price)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                product.description?.let { desc ->
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                                
//                                product.categoryName?.let { category ->
//                                    Spacer(modifier = Modifier.height(4.dp))
//                                    Text(
//                                        text = "📁 $category",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.secondary
//                                    )
//                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ID: ${product.productId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

