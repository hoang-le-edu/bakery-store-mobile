package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.CartViewModel
import com.dev.thecodecup.model.network.dto.ProductDto
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun ProductDetailScreen(
    product: ProductDto,
    onBack: () -> Unit,
    onAddToCart: (CartItemEntity) -> Unit,
    cartViewModel: CartViewModel
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableIntStateOf(0) }
    var selectedShot by remember { mutableIntStateOf(0) }
    var selectedIce by remember { mutableIntStateOf(0) }

    val sizes = listOf("Small", "Medium", "Large")
    val shots = listOf("Single", "Double")
    val ices = listOf("Little", "Medium", "Full")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF324A59)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Detail",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = poppinsFontFamily
                    )
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = product.productImage ?: "",
                    contentDescription = product.productName,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🍰",
                                fontSize = 100.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Product Name and Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = poppinsFontFamily
                                )
                            )
                            if (!product.productDescription.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = product.productDescription,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray,
                                        fontFamily = poppinsFontFamily
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price
                    Text(
                        text = "${product.price.toInt().toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ",")}đ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF324A59),
                            fontFamily = poppinsFontFamily
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quantity Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quantity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = poppinsFontFamily
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { if (quantity > 1) quantity-- },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0E0E0),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = poppinsFontFamily
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Button(
                                onClick = { quantity++ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF324A59),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Add to Cart Button
                    Button(
                        onClick = {
                            val cartItem = CartItemEntity(
                                name = product.productName,
                                price = product.price,
                                imageUrl = product.productImage,
                                size = sizes[selectedSize],
                                shot = shots[selectedShot],
                                ice = ices[selectedIce],
                                quantity = quantity,
                                point = (product.price / 1000).toInt()
                            )
                            onAddToCart(cartItem)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF324A59)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Add to Cart",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = poppinsFontFamily
                            )
                        )
                    }
                }
            }
        }
    }
}

