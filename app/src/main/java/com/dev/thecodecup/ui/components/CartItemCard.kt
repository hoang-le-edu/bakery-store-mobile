package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun CartItemCard(
    cartItem: CartItemEntity,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF7F8FB),
                shape = RoundedCornerShape(16.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .size(90.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cartItem.imageUrl != null) {
                SubcomposeAsyncImage(
                    model = cartItem.imageUrl,
                    contentDescription = cartItem.name,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF324A59)
                            )
                        }
                    },
                    error = {
                        Text(
                            text = "🍰",
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            } else if (cartItem.imageResId != 0) {
                Image(
                    painter = painterResource(cartItem.imageResId),
                    contentDescription = cartItem.name,
                    modifier = Modifier.size(80.dp)
                )
            } else {
                Text(
                    text = "🍰",
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cartItem.name,
                color = Color(0xFF001833),
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Text(
                text = "${cartItem.shot} | ${cartItem.size} | ${cartItem.ice}",
                color = Color(0xFF757575),
                fontFamily = poppinsFontFamily,
                fontSize = 10.sp
            )
            Text(
                text = "x ${cartItem.quantity}",
                color = Color(0xFF757575),
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
        }
        Box(
            modifier = Modifier
                .padding(8.dp),
            contentAlignment = Alignment.CenterEnd
        ){
            Text(
                text = "${cartItem.price.toInt().toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ",")}đ",
                color = Color(0xFF001833),
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
        }
    }
}
