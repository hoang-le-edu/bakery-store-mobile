package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import com.dev.thecodecup.model.item.CoffeeItem
import com.dev.thecodecup.model.item.FilterPreferences
import com.dev.thecodecup.model.network.dto.ProductDto
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel
import com.dev.thecodecup.ui.components.CoffeeCard
import com.dev.thecodecup.ui.components.HomeHeader
import com.dev.thecodecup.ui.components.LoyaltyCard
import com.dev.thecodecup.ui.components.BottomNavBar
import com.dev.thecodecup.ui.components.ProductCard
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import com.dev.thecodecup.model.db.user.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    coffeeList: List<CoffeeItem>,
    onCoffeeClick: (CoffeeItem) -> Unit,
    onProductClick: (ProductDto) -> Unit,
    onNavClick: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    showEmptyCartDialog: Boolean = false,
    onDismissEmptyCartDialog: () -> Unit = {},
    activeFilters: FilterPreferences? = null
) {
    val user by userViewModel.user.collectAsState()
    val userName = user?.name?.split(" ")?.firstOrNull() ?: "User"
    val userStamps = user?.stamp ?: 0
    
    // Product state from API
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.error.collectAsState()
    
    // Load products on first composition
    LaunchedEffect(Unit) {
        productViewModel.loadProducts(
            limit = null,
            searchText = "",
            categoryId = "all"
        )
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (showEmptyCartDialog) {
                AlertDialog(
                    onDismissRequest = onDismissEmptyCartDialog,
                    title = { Text(
                        text = "Cart is empty",
                        fontFamily = poppinsFontFamily
                    ) },
                    text = { Text(text = "Please add something to your cart before checking out.",
                        fontFamily = poppinsFontFamily
                    ) },
                    confirmButton = {
                        Button(
                            onClick = onDismissEmptyCartDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF324A59),
                                contentColor = Color.White
                            ),
                            ) {
                            Text(
                                text = "Confirm",
                                fontFamily = poppinsFontFamily
                            )
                        }
                    }
                )
            }
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                HomeHeader(
                    userName = userName,
                    onNavClick = onNavClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                HomeSearchBar(
                    onSearchClick = onSearchClick,
                    onFilterClick = onFilterClick,
                    activeFilters = activeFilters
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LoyaltyCard(
                        currentStamps = userStamps,
                        maxStamps = 8,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Surface(
                    color = Color(0xFF324A59),
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp,
                                start = 16.dp,
                                end = 16.dp)
                    ) {
                        Text(
                            text = "Choose your product",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFD8D8D8),
                                fontFamily = poppinsFontFamily
                            ),
                            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show loading indicator
                        if (isLoading && products.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFD8D8D8),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        // Show error message
                        else if (error != null && products.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "⚠️",
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Text(
                                        text = error ?: "Failed to load products",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFFD8D8D8)
                                        ),
                                        fontFamily = poppinsFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { productViewModel.refresh() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD8D8D8),
                                            contentColor = Color(0xFF324A59)
                                        )
                                    ) {
                                        Text("Retry", fontFamily = poppinsFontFamily)
                                    }
                                }
                            }
                        }
                        // Show products from API
                        else if (products.isNotEmpty()) {
                            LazyVerticalGrid (
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                items(products) { product ->
                                    ProductCard(
                                        product = product,
                                        onClick = { onProductClick(product) }
                                    )
                                }
                            }
                        }
                        // Fallback to static coffee list if no API data
                        else {
                            LazyVerticalGrid (
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                items(coffeeList) { coffee ->
                                    CoffeeCard(coffee = coffee, onClick = { onCoffeeClick(coffee) })
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .width(350.dp)
            ) {
                BottomNavBar(
                    currentRoute = "home",
                    onItemSelected = onNavClick
                )
            }
        }
    }
}

@Composable
private fun HomeSearchBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    activeFilters: FilterPreferences?
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF4F5F7)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSearchClick() },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF324A59)
                        )
                    }
                    Column {
                        Text(
                            text = "Search menu",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF324A59),
                                fontFamily = poppinsFontFamily
                            )
                        )
                        Text(
                            text = "Coffee, pastry, more",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF7A8A99),
                                fontFamily = poppinsFontFamily
                            )
                        )
                    }
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filters",
                        tint = Color(0xFF324A59)
                    )
                }
            }
        }
        if (activeFilters != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = onFilterClick,
                label = {
                    Text(
                        text = "₫${activeFilters.priceRange.start.toInt()} - ₫${activeFilters.priceRange.endInclusive.toInt()} | ${activeFilters.minRating}★+",
                        fontFamily = poppinsFontFamily
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFE8EEF2)
                )
            )
        }
    }
}