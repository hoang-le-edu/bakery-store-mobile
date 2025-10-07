package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.model.data.redeemCoffeeList
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.order.OrderViewModel
import com.dev.thecodecup.model.item.CoffeeItem
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.dev.thecodecup.model.db.user.UserViewModel

@Composable
fun RedeemScreen(
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit
){
    val user = userViewModel.user.collectAsState().value
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val snackHostState = remember { SnackbarHostState() }
            var showNotEnoughPoints by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(38.dp))
                Header(
                    title = "Redeem",
                    onBack = onBack,
                    haveBack = true
                )

                LazyColumn (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    items(redeemCoffeeList.size) {
                        val coffee = redeemCoffeeList[it]
                        Row (
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Image(
                                painter = painterResource(coffee.imageResId),
                                contentDescription = coffee.name,
                                modifier = Modifier
                                    .height(90.dp)
                                    .weight(0.3f)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(horizontal = 8.dp),
                            ) {
                                Text(
                                    text = coffee.name,
                                    color = Color(0xFF324A59),
                                    fontFamily = poppinsFontFamily,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Valid until 06.07.2025",
                                    color = Color(0xFF324A59).copy(alpha = 0.5f),
                                    fontFamily = poppinsFontFamily,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = if((user?.point ?: 0) >= coffee.points) {
                                    {
                                        val now = LocalDateTime.now()
                                        val formatter = DateTimeFormatter.ofPattern("d MMMM | hh:mm a", Locale.ENGLISH)
                                        val formatted = now.format(formatter)
                                        orderViewModel.insertOrder(
                                            OrderEntity(
                                                cartItem = CartItemEntity(
                                                    coffee = CoffeeItem(
                                                        id = coffee.id,
                                                        name = coffee.name,
                                                        imageResId = coffee.imageResId,
                                                        price = 0.0
                                                    ),
                                                    haveIced = true,
                                                    point = 0
                                                ),
                                                orderTime = formatted,
                                                location = user?.address ?: ""
                                            )
                                        )
                                        user?.let {
                                            userViewModel.updateUser(it.copy(point = it.point - coffee.points))
                                        }
                                    }
                                } else {
                                    {
                                        showNotEnoughPoints = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(0.3f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if((user?.point ?: 0) >= coffee.points)
                                        Color(0xFF324A59)
                                    else
                                        Color(0xFF324A59).copy(alpha = 0.5f),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "${coffee.points} pts",
                                    fontFamily = poppinsFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                if (showNotEnoughPoints) {
                    LaunchedEffect(Unit) {
                        snackHostState.showSnackbar("You don't have enough points")
                        showNotEnoughPoints = false
                    }
                }
            }
        }

    }
}