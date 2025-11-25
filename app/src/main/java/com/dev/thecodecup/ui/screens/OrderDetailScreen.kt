package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.order.OrderViewModel
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun OrderDetailScreen(
    orderId: Int,
    orderViewModel: OrderViewModel,
    onBack: () -> Unit = {},
    onOrderCancelled: () -> Unit = {}
) {
    val order by orderViewModel.selectedOrder.collectAsState()

    LaunchedEffect(orderId) {
        orderViewModel.loadOrderById(orderId)
    }

    DisposableEffect(Unit) {
        onDispose { orderViewModel.clearSelectedOrder() }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Order detail", onBack = onBack, haveBack = true)

            if (order == null) {
                Spacer(modifier = Modifier.height(48.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading order...",
                        fontFamily = poppinsFontFamily
                    )
                }
            } else {
                OrderDetailContent(
                    order = order!!,
                    onCancel = {
                        orderViewModel.deleteOrder(order!!)
                        onOrderCancelled()
                    }
                )
            }
        }
    }
}

@Composable
private fun OrderDetailContent(order: OrderEntity, onCancel: () -> Unit) {
    val cartItem = order.toCartItem()
    Spacer(modifier = Modifier.height(24.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = cartItem.name,
                fontFamily = poppinsFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = order.orderTime,
                fontFamily = poppinsFontFamily,
                color = Color(0xFF7A8A99)
            )
            Divider(color = Color(0xFFE0E0E0))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Configuration",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Shot: ${cartItem.shot}\nSize: ${cartItem.size}\nIce: ${cartItem.ice}",
                        fontFamily = poppinsFontFamily,
                        color = Color(0xFF7A8A99)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Quantity",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${cartItem.quantity}",
                        fontFamily = poppinsFontFamily,
                        color = Color(0xFF7A8A99)
                    )
                }
            }
            Divider(color = Color(0xFFE0E0E0))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Delivery to",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = order.location,
                        fontFamily = poppinsFontFamily,
                        color = Color(0xFF7A8A99)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$${"%.2f".format(cartItem.price * cartItem.quantity)}",
                        fontFamily = poppinsFontFamily,
                        fontSize = 20.sp,
                        color = Color(0xFF324A59),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Timeline",
        fontFamily = poppinsFontFamily,
        fontSize = 18.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
    val steps = if (order.isHistory) listOf("Ordered", "Prepared", "Completed") else listOf("Ordered", "In progress", "Ready to ship")
    steps.forEachIndexed { index, step ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = step,
                fontFamily = poppinsFontFamily,
                color = if (index <= 1 || order.isHistory) Color(0xFF324A59) else Color(0xFF7A8A99)
            )
            Text(
                text = if (index == 0) order.orderTime else "--",
                fontFamily = poppinsFontFamily,
                color = Color(0xFF7A8A99)
            )
        }
        Divider(color = Color(0xFFE0E0E0))
    }

    if (!order.isHistory) {
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFDECEC),
                contentColor = Color(0xFFB00020)
            )
        ) {
            Text(
                text = "Cancel order",
                fontFamily = poppinsFontFamily,
                fontSize = 16.sp
            )
        }
    }
}
