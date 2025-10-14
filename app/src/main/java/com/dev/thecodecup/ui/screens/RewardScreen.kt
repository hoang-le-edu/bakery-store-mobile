package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.ui.components.BottomNavBar
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.components.LoyaltyCard
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import com.dev.thecodecup.R
import com.dev.thecodecup.model.db.order.OrderViewModel
import com.dev.thecodecup.model.db.user.UserViewModel

@Composable
fun RewardScreen(
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
    onNavClick: (String) -> Unit = {}
) {
    val historyOrderList = orderViewModel.historyOrders.collectAsState().value
    val user = userViewModel.user.collectAsState().value
    val userStamps = user?.stamp ?: 0
    var showDialog by remember { mutableStateOf(false) }
    var isFullStamps by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        paddingValues ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(38.dp))
                Header(
                    title = "Rewards"
                )
                LoyaltyCard(
                    currentStamps = userStamps,
                    onClick = {
                        isFullStamps = userStamps == 8
                        showDialog = true
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .clip(RoundedCornerShape(16.dp))
                ){
                    Image(
                        painter = painterResource(R.drawable.redeem_bg),
                        contentDescription = "Redeem Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Row (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 16.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Column {
                            Text(
                                text = "My Points:",
                                color = Color(0xFFD8D8D8),
                                fontFamily = poppinsFontFamily,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "${user?.point ?: 0}",
                                color = Color(0xFFD8D8D8),
                                fontFamily = poppinsFontFamily,
                                fontSize = 28.sp
                            )
                        }
                        TextButton(
                            onClick = { onNavClick("redeem") },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFA2CDE9).copy(alpha = 0.19f),
                                contentColor = Color(0xFFD8D8D8)
                            ),
                        ) {
                            Text(
                                text = "Redeem drinks",
                                fontFamily = poppinsFontFamily,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Text(
                    text = "History Rewards",
                    color = Color(0xFF324A59),
                    fontFamily = poppinsFontFamily,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyOrderList.size) {
                        val rewards = historyOrderList[it]
                        if(rewards.cartItem.point <= 0) return@items
                        Spacer(modifier = Modifier.height(8.dp))
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ){
                                Text(
                                    text = rewards.cartItem.name,
                                    color = Color(0xFF324A59),
                                    fontFamily = poppinsFontFamily,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = rewards.orderTime,
                                    color = Color(0xFF324A59).copy(alpha = 0.4f),
                                    fontFamily = poppinsFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "+ ${rewards.cartItem.point * rewards.cartItem.quantity} Pts",
                                color = Color(0xFF324A59),
                                fontFamily = poppinsFontFamily,
                                fontSize = 20.sp,
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color(0xFFD8D8D8),
                            thickness = 0.6.dp
                        )
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(if (isFullStamps) "Redeem Reward" else "Not Enough Stamps",
                        fontFamily = poppinsFontFamily) },
                    text = {
                        Text(
                            if (isFullStamps)
                                "You have 8 stamps. Would you like to use them for a reward?"
                            else
                                "You need 8 stamps to redeem a reward. Buy more to collect stamps!",
                            fontFamily = poppinsFontFamily
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                if( isFullStamps) {
                                    user?.let {
                                        userViewModel.updateUser(it.copy(stamp = 0))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF324A59),
                                contentColor = Color.White
                            )
                        ) {
                            Text("OK",
                                fontFamily = poppinsFontFamily
                            )
                        }
                    },
                    dismissButton = if (isFullStamps) {
                        {
                            TextButton(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF324A59),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Cancel",
                                    fontFamily = poppinsFontFamily
                                )
                            }
                        }
                    } else null
                )
            }

            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .width(350.dp)
                    .align(Alignment.BottomCenter)
                    .border(
                        width = 0.6.dp,
                        color = Color(0xFFD8D8D8),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                BottomNavBar(
                    currentRoute = "rewards",
                    onItemSelected = onNavClick
                )
            }
        }
    }
}
