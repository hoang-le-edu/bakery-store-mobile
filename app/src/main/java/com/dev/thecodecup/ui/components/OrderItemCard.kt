package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.R
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun OrderItemCard(
    orderItem: OrderEntity,
    isHistoryCard: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val alphaValue = if (isHistoryCard) 0.4f else 1.0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val cartItem = orderItem.cartItem
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = orderItem.orderTime,
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp,
                    color = Color(0xFF324A59).copy(alpha = 0.4f * alphaValue),
                )

                Text(
                    text = "${cartItem.price.toInt().toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ",")}đ",
                    fontFamily = poppinsFontFamily,
                    fontSize = 24.sp,
                    color = Color(0xFF324A59).copy(alpha = alphaValue)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_small),
                    contentDescription = "Product Name",
                    modifier = Modifier
                        .size(25.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF324A59).copy(alpha = 0.8f * alphaValue))
                )

                Text(
                    text = cartItem.name,
                    fontSize = 16.sp,
                    fontFamily = poppinsFontFamily,
                    color = Color(0xFF324A59).copy(alpha = alphaValue),
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = "Coffee Name",
                    modifier = Modifier
                        .size(25.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF324A59).copy(alpha = 0.8f * alphaValue))
                )

                Text(
                    text = orderItem.location,
                    fontSize = 16.sp,
                    fontFamily = poppinsFontFamily,
                    color = Color(0xFF324A59).copy(alpha = alphaValue),
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFFF4F5F7).copy(alpha = alphaValue),
                thickness = 1.dp
            )
        }
    }
}
