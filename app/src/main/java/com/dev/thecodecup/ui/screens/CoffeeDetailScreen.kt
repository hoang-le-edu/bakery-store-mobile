package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dev.thecodecup.R
import com.dev.thecodecup.model.item.CoffeeItem
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.components.QuantitySelector
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.CartViewModel
import com.dev.thecodecup.model.db.cart.CoffeeSize
import com.dev.thecodecup.model.db.cart.IceLevel
import com.dev.thecodecup.model.db.cart.ShotLevel
import com.dev.thecodecup.ui.components.ImageOptionSelector
import com.dev.thecodecup.ui.components.TextOptionSelector
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@Composable
fun CoffeeDetailScreen(
    coffeeItem: CoffeeItem,
    onBack: () -> Unit = {},
    onAddToCart: (CartItemEntity) -> Unit = {},
    cartViewModel: CartViewModel
) {
    val cartItems = cartViewModel.cartItems.collectAsState().value
    var showCartDialog by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    var shotLevel by remember { mutableStateOf(ShotLevel.SINGLE) }
    var haveIce by remember { mutableStateOf(false) }
    var coffeeSize by remember { mutableStateOf(CoffeeSize.MEDIUM) }
    var iceLevel by remember { mutableStateOf(IceLevel.NORMAL) }
    
    // Unit price per item (after size/shot adjustments, before quantity)
    val unitPrice = coffeeItem.price * when (coffeeSize) {
        CoffeeSize.SMALL -> 0.9
        CoffeeSize.MEDIUM -> 1.0
        CoffeeSize.LARGE -> 1.1
    } * when (shotLevel) {
        ShotLevel.SINGLE -> 1.0
        ShotLevel.DOUBLE -> 1.5
    }
    
    // Total price for display (unit price * quantity)
    val totalPrice = unitPrice * quantity
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Header(
                    title = "Details",
                    onBack = onBack,
                    onCart = { showCartDialog = true },
                    haveBack = true,
                    haveCart = true
                )
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(
                            width = 325.dp,
                            height = 145.dp
                        )
                        .background(
                            color = Color(0xFFF7F8FB),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(coffeeItem.imageResId),
                        contentDescription = coffeeItem.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                QuantitySelector(
                    title = coffeeItem.name,
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(
                        vertical = 6.dp,
                        horizontal = 16.dp)
                )
                TextOptionSelector(
                    title = "Shot",
                    options = listOf("Single", "Double"),
                    selected = if (shotLevel == ShotLevel.SINGLE) "Single"
                    else "Double",
                    onSelect = {
                        shotLevel = if (it == "Single") ShotLevel.SINGLE
                        else ShotLevel.DOUBLE
                    }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(
                        vertical = 6.dp,
                        horizontal = 16.dp)
                )
                ImageOptionSelector(
                    title = "Select",
                    options = listOf(
                        "NoIce" to R.drawable.ic_stay,
                        "HaveIce" to R.drawable.ic_takeaway
                    ),
                    selected = if (haveIce == true) "HaveIce"
                    else "NoIce",
                    onSelect =  { haveIce = it == "HaveIce" }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(
                        vertical = 6.dp,
                        horizontal = 16.dp)
                )
                ImageOptionSelector(
                    title = "Size",
                    options = listOf(
                        "Small" to R.drawable.ic_small,
                        "Medium" to R.drawable.ic_medium,
                        "Large" to R.drawable.ic_large
                    ),
                    selected = if (coffeeSize == CoffeeSize.SMALL) "Small"
                    else if (coffeeSize == CoffeeSize.MEDIUM) "Medium"
                    else "Large",
                    onSelect =  { coffeeSize = when (it) {
                        "Small" -> CoffeeSize.SMALL
                        "Medium" -> CoffeeSize.MEDIUM
                        else -> CoffeeSize.LARGE
                    }
                    },
                    isSize = true
                )
                if(haveIce) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(
                            vertical = 6.dp,
                            horizontal = 16.dp)
                    )
                    ImageOptionSelector(
                        title = "Ice",
                        options = listOf(
                            "Less" to R.drawable.ic_ice30,
                            "Normal" to R.drawable.ic_ice40,
                            "Extra" to R.drawable.ic_ice50
                        ),
                        selected = when (iceLevel) {
                            IceLevel.LESS -> "Less"
                            IceLevel.NORMAL -> "Normal"
                            else -> "Extra"
                        },
                        onSelect = { iceLevel = when (it) {
                            "Less" -> IceLevel.LESS
                            "Normal" -> IceLevel.NORMAL
                            else -> IceLevel.EXTRA
                        } },
                        isIce = true
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = "Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            color = Color(0xFF324A59),
                            fontFamily = poppinsFontFamily
                        )
                        Text(
                            text = "$${"%.2f".format(totalPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            color = Color(0xFF324A59),
                            fontFamily = poppinsFontFamily
                        )
                    }
                    Button(
                        onClick = {
                            onAddToCart(
                                CartItemEntity(
                                    name = coffeeItem.name,
                                    price = unitPrice,  // Store unit price, not total
                                    imageResId = coffeeItem.imageResId,
                                    imageUrl = null,
                                    shot = when(shotLevel) {
                                        ShotLevel.SINGLE -> "Single"
                                        ShotLevel.DOUBLE -> "Double"
                                    },
                                    size = when(coffeeSize) {
                                        CoffeeSize.SMALL -> "Small"
                                        CoffeeSize.MEDIUM -> "Medium"
                                        CoffeeSize.LARGE -> "Large"
                                    },
                                    ice = when(iceLevel) {
                                        IceLevel.LESS -> "Less"
                                        IceLevel.NORMAL -> "Normal"
                                        IceLevel.EXTRA -> "Extra"
                                    },
                                    quantity = quantity,
                                    point = (unitPrice / 1000).toInt()
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF324A59),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Add to cart",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = poppinsFontFamily
                        )
                    }
                }
            }
        }
        if (showCartDialog) {
            AlertDialog(
                onDismissRequest = { showCartDialog = false },
                title = { Text(
                    text = "Your Cart",
                    fontFamily = poppinsFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ) },
                text = {
                    if (cartItems.isEmpty()) {
                        Text(
                            text = "Your cart is empty.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFF324A59),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column {
                            cartItems.forEach { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (item.imageResId != 0) {
                                            Image(
                                                painter = painterResource(item.imageResId),
                                                contentDescription = item.name,
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(end = 8.dp)
                                            )
                                        }
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            fontFamily = poppinsFontFamily
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("x${item.quantity}")
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showCartDialog = false },
                        colors = ButtonDefaults
                            .buttonColors(
                                containerColor = Color(0xFF324A59),
                                contentColor = Color.White
                            )
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = poppinsFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    }
}