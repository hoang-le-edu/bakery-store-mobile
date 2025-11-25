package com.dev.thecodecup.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.order.OrderViewModel
import kotlin.math.roundToInt

@Composable
fun OnGoingList(
    orderViewModel: OrderViewModel,
    onOrderSelected: (OrderEntity) -> Unit = {}
) {
    val onGoingOrderList = orderViewModel.ongoingOrders.collectAsState().value
    val buttonWidth = 100.dp
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { buttonWidth.toPx() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(onGoingOrderList.size) { index ->
            Spacer(modifier = Modifier.height(8.dp))
            val order = onGoingOrderList[index]
            var offsetX by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                val animatedButtonWidth by animateDpAsState(targetValue = if (offsetX == maxOffsetPx) buttonWidth else 0.dp, label = "buttonWidth")
                val animatedAlpha by animateFloatAsState(targetValue = if (offsetX == maxOffsetPx) 1f else 0f, label = "buttonAlpha")
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(animatedButtonWidth)
                        .fillMaxHeight(0.8f)
                        .background(
                            color = Color(0xFFE5FFEA),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .alpha(animatedAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    if (animatedButtonWidth > 0.dp) {
                        Button(
                            onClick = {
                                offsetX = 0.0f
                                orderViewModel.updateOrder(order.copy(isHistory = true))
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 4.dp,
                                    bottom = 4.dp
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accept",
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .offset { IntOffset(-offsetX.roundToInt(), 0) }
                        .fillMaxWidth()
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
                                    val newOffset = (offsetX + dragAmount * -1).coerceIn(0f, maxOffsetPx)
                                    offsetX = newOffset
                                }
                            )
                        }
                ) {
                    OrderItemCard(
                        orderItem = order,
                        onClick = { onOrderSelected(order) }
                    )
                }
            }
        }
    }
}
