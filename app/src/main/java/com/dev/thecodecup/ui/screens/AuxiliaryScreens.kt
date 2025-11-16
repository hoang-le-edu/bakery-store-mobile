package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.item.FilterPreferences
import com.dev.thecodecup.model.item.SortOption
import com.dev.thecodecup.model.network.dto.ProductDto
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.components.ProductCard
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun SearchScreen(
    productViewModel: ProductViewModel,
    onProductClick: (ProductDto) -> Unit,
    onBack: () -> Unit = {},
    onFilterClick: () -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    val products by productViewModel.products.collectAsState()

    LaunchedEffect(Unit) {
        if (products.isEmpty()) {
            productViewModel.loadProducts(limit = null, searchText = "", categoryId = "all")
        }
    }

    val filtered = products.filter { dto ->
        dto.productName.contains(query, ignoreCase = true)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Search", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search products", fontFamily = poppinsFontFamily) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear")
                        }
                    } else {
                        IconButton(onClick = onFilterClick) {
                            Icon(imageVector = Icons.Outlined.FilterAlt, contentDescription = "Filter")
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.productId }) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product) })
                }
            }
        }
    }
}

@Composable
fun FilterSortScreen(
    initialFilters: FilterPreferences = FilterPreferences(),
    onBack: () -> Unit = {},
    onApply: (FilterPreferences) -> Unit = {}
) {
    var priceRange by rememberSaveable { mutableStateOf(initialFilters.priceRange.start..initialFilters.priceRange.endInclusive) }
    var minRating by rememberSaveable { mutableStateOf(initialFilters.minRating) }
    var sortOption by rememberSaveable { mutableStateOf(initialFilters.sortOption) }
    var onlyInStock by rememberSaveable { mutableStateOf(initialFilters.onlyInStock) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Filters", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Price range", fontFamily = poppinsFontFamily)
            RangeSlider(
                value = priceRange,
                valueRange = 0f..100f,
                onValueChange = { priceRange = it }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("₫${priceRange.start.toInt()}", fontFamily = poppinsFontFamily)
                Text("₫${priceRange.endInclusive.toInt()}", fontFamily = poppinsFontFamily)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Minimum rating", fontFamily = poppinsFontFamily)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (3..5).forEach { rating ->
                    RatingChip(value = rating, selected = rating == minRating) { minRating = rating }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Sort by", fontFamily = poppinsFontFamily)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SortOption.entries.forEach { option ->
                    SortRow(
                        title = option.asLabel(),
                        selected = sortOption == option,
                        onClick = { sortOption = option }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            SortRow(
                title = "Show only available items",
                selected = onlyInStock,
                onClick = { onlyInStock = !onlyInStock },
                showRadio = false
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onApply(
                        FilterPreferences(
                            priceRange = priceRange,
                            minRating = minRating,
                            sortOption = sortOption,
                            onlyInStock = onlyInStock
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF324A59), contentColor = Color.White)
            ) {
                Text(text = "Apply filters", fontFamily = poppinsFontFamily)
            }
        }
    }
}

@Composable
fun ChatScreen(onBack: () -> Unit = {}) {
    val messages = remember { mutableStateListOf(
        Message(author = "Support", body = "Hi! How can we help?"),
        Message(author = "You", body = "Need help with my last order")
    ) }
    var input by rememberSaveable { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Support chat", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message", fontFamily = poppinsFontFamily) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF4F5F7),
                        unfocusedContainerColor = Color(0xFFF4F5F7),
                        disabledContainerColor = Color(0xFFE7E7E7)
                    )
                )
                IconButton(onClick = {
                    if (input.isNotBlank()) {
                        messages.add(Message(author = "You", body = input.trim()))
                        input = ""
                    }
                }) {
                    Icon(imageVector = Icons.Outlined.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun LanguageSettingsScreen(onBack: () -> Unit = {}) {
    var selectedLanguage by rememberSaveable { mutableStateOf("en") }
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Language", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(16.dp))
            listOf("en" to "English", "vi" to "Tiếng Việt", "ja" to "日本語").forEach { (code, label) ->
                SortRow(
                    title = label,
                    selected = selectedLanguage == code,
                    onClick = { selectedLanguage = code }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RatingChip(value: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF324A59) else Color(0xFFF4F5F7)
    ) {
        Text(
            text = "$value★",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable { onClick() },
            color = if (selected) Color.White else Color(0xFF001833),
            fontFamily = poppinsFontFamily
        )
    }
}

@Composable
private fun SortRow(title: String, selected: Boolean, onClick: () -> Unit, showRadio: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF324A59) else Color(0xFFF4F5F7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = poppinsFontFamily,
                color = if (selected) Color.White else Color(0xFF001833)
            )
            if (showRadio) {
                RadioButton(selected = selected, onClick = onClick)
            } else {
                SwitchChip(isChecked = selected, onToggle = onClick)
            }
        }
    }
}

@Composable
private fun SwitchChip(isChecked: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isChecked) Color(0xFF324A59) else Color.White
    ) {
        Text(
            text = if (isChecked) "On" else "Off",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onToggle() },
            color = if (isChecked) Color.White else Color(0xFF001833),
            fontFamily = poppinsFontFamily
        )
    }
}

@Composable
private fun ChatBubble(message: Message) {
    val isSender = message.author == "You"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isSender) Color(0xFF324A59) else Color(0xFFF4F5F7)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.author,
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    color = if (isSender) Color(0xFFE0E0E0) else Color(0xFF7A8A99)
                )
                Text(
                    text = message.body,
                    fontFamily = poppinsFontFamily,
                    color = if (isSender) Color.White else Color(0xFF001833)
                )
            }
        }
    }
}

private data class Message(val author: String, val body: String)

private fun SortOption.asLabel(): String = when (this) {
    SortOption.POPULARITY -> "Popularity"
    SortOption.PRICE_LOW_HIGH -> "Price: Low to High"
    SortOption.PRICE_HIGH_LOW -> "Price: High to Low"
    SortOption.RATING -> "Rating"
}
