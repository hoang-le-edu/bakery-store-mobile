package com.dev.thecodecup.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.CartViewModel
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.order.OrderViewModel
import com.dev.thecodecup.model.db.user.UserViewModel
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    onBack: () -> Unit = {},
    onCheckoutSuccess: () -> Unit = {}
) {
    val cartItems = cartViewModel.cartItems.collectAsState().value
    val user = userViewModel.user.collectAsState().value

    var selectedAddress by rememberSaveable { mutableStateOf(user?.address ?: "") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedPayment by rememberSaveable { mutableStateOf("Card") }

    LaunchedEffect(user) {
        selectedAddress = user?.address ?: ""
    }

    val context = LocalContext.current
    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    fun placeOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val formatter = DateTimeFormatter.ofPattern("d MMMM | hh:mm a", Locale.ENGLISH)
        var newPoint = user?.point ?: 0
        cartItems.forEach { item ->
            val orderEntity = OrderEntity.fromCartItem(
                cartItem = item,
                location = selectedAddress.ifEmpty { user?.address ?: "Store" },
                orderTime = LocalDateTime.now().format(formatter)
            )
            orderViewModel.insertOrder(orderEntity)
            newPoint += item.point * item.quantity
        }
        user?.let {
            val newStamp = min(it.stamp + cartItems.size, 8)
            userViewModel.updateUser(it.copy(point = newPoint, stamp = newStamp))
        }
        cartViewModel.clearCart()
        onCheckoutSuccess()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Checkout", onBack = onBack, haveBack = true)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Delivery",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = user?.name ?: "Guest",
                        fontFamily = poppinsFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedAddress.ifEmpty { "Add your address" },
                        fontFamily = poppinsFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF7A8A99)
                    )
                    OutlinedTextField(
                        value = selectedAddress,
                        onValueChange = { selectedAddress = it },
                        label = { Text("Delivery address", fontFamily = poppinsFontFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Delivery note", fontFamily = poppinsFontFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Payment method",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            PaymentOptionRow(
                title = "Credit / Debit Card",
                subtitle = "Visa •••• 7281",
                isSelected = selectedPayment == "Card",
                onSelect = { selectedPayment = "Card" }
            )
            PaymentOptionRow(
                title = "Cash on delivery",
                subtitle = "Pay when received",
                isSelected = selectedPayment == "COD",
                onSelect = { selectedPayment = "COD" }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Order summary",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (cartItems.isEmpty()) {
                        Text(
                            text = "No items in cart",
                            fontFamily = poppinsFontFamily,
                            color = Color(0xFF7A8A99)
                        )
                    } else {
                        cartItems.forEach { item ->
                            OrderSummaryRow(item = item)
                        }
                    }
                    Divider(color = Color(0xFFE0E0E0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            fontFamily = poppinsFontFamily,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$${"%.2f".format(totalPrice)}",
                            fontFamily = poppinsFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { placeOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF324A59),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Place order",
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF324A59) else Color(0xFFF4F5F7)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp,
                    color = if (isSelected) Color.White else Color(0xFF001833)
                )
                Text(
                    text = subtitle,
                    fontFamily = poppinsFontFamily,
                    fontSize = 14.sp,
                    color = if (isSelected) Color(0xFFE0E0E0) else Color(0xFF7A8A99)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color(0xFF7A8A99)
                )
            )
        }
    }
}

@Composable
private fun OrderSummaryRow(item: CartItemEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontFamily = poppinsFontFamily,
                fontSize = 16.sp
            )
            Text(
                text = "x${item.quantity}",
                fontFamily = poppinsFontFamily,
                fontSize = 14.sp,
                color = Color(0xFF7A8A99)
            )
        }
        Text(
            text = "$${"%.2f".format(item.price * item.quantity)}",
            fontFamily = poppinsFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
