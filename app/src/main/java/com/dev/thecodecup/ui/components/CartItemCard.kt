package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.CoffeeSize
import com.dev.thecodecup.model.db.cart.IceLevel
import com.dev.thecodecup.model.db.cart.ShotLevel
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun CartItemCard(
    cartItem: CartItemEntity,
){
    var coffeePrice = cartItem.coffee.price * cartItem.quantity * when (cartItem.size) {
        CoffeeSize.SMALL -> 0.9
        CoffeeSize.MEDIUM -> 1.0
        CoffeeSize.LARGE -> 1.1
    } * when (cartItem.shot) {
        ShotLevel.SINGLE -> 1.0
        ShotLevel.DOUBLE -> 1.5
    }
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
        Image(
            painter = painterResource(cartItem.coffee.imageResId),
            contentDescription = cartItem.coffee.name,
            modifier = Modifier
                .size(90.dp)
                .padding(horizontal = 4.dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = cartItem.coffee.name,
                color = Color(0xFF001833),
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Text(
                text = when(cartItem.shot) {
                    ShotLevel.SINGLE -> "single"
                    ShotLevel.DOUBLE -> "double"
                } + " | ${
                    when(cartItem.haveIced) {
                        true -> "iced"
                        false -> "hot"
                    }
                }" + " | ${
                    when(cartItem.size) {
                        CoffeeSize.SMALL -> "small"
                        CoffeeSize.MEDIUM -> "medium"
                        CoffeeSize.LARGE -> "large"
                    }
                }" + if(cartItem.haveIced) " | ${
                    when(cartItem.ice) {
                        IceLevel.LESS -> "less ice"
                        IceLevel.NORMAL -> "normal ice"
                        IceLevel.EXTRA -> "full ice"
                    }
                }" else "",
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
                text = "$${"%.2f".format(coffeePrice)}",
                color = Color(0xFF001833),
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
        }
    }
}
