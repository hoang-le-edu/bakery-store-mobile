package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.R
import com.dev.thecodecup.model.db.cart.CartViewModel
import com.dev.thecodecup.ui.components.CartItemCard
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import kotlin.math.roundToInt

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    cartViewModel: CartViewModel
) {
    val cartItems = cartViewModel.cartItems.collectAsState().value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_arrowback),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "My Cart",
                    color = Color(0xFF001833),
                    fontFamily = poppinsFontFamily,
                    fontSize = 32.sp
                )
            }

            cartItems.forEach { cartItem ->
                val buttonWidth = 80.dp
                val density = LocalDensity.current
                val maxOffsetPx = with(density) { buttonWidth.toPx() }
                var offsetX by remember { mutableFloatStateOf(0f) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(buttonWidth)
                            .fillMaxSize()
                            .background(
                                color = Color(0xFFFFE5E5),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                cartViewModel.removeCartItem(cartItem)
                                offsetX = 0.0f
                            },
                            modifier = Modifier
                                .fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFE5E5),
                                contentColor = Color(0xFFB00020)
                            ),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(50.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(-offsetX.roundToInt(), 0) }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        offsetX = if (offsetX > maxOffsetPx / 2) {
                                            maxOffsetPx
                                        } else {
                                            0f
                                        }
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        val newOffset =
                                            (offsetX + dragAmount * -1).coerceIn(0f, maxOffsetPx)
                                        offsetX = newOffset
                                    }
                                )
                            }
                    ) {
                        CartItemCard(cartItem = cartItem)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Total Price",
                    color = Color(0xFF001833)
                        .copy(alpha = 0.22f),
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp
                )
                Text(
                    text = "$${"%.2f".format(cartViewModel.getCartPrice())}",
                    color = Color(0xFF001833),
                    fontFamily = poppinsFontFamily,
                    fontSize = 24.sp
                )
            }
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF324A59),
                    contentColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight(),
                ) {
                    Image(
                        painter = painterResource(R.drawable.buy),
                        contentDescription = "Checkout",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Text(text = "Checkout",
                        modifier = Modifier.padding(start = 8.dp)
                            .align(Alignment.CenterVertically),
                        fontFamily = poppinsFontFamily,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
