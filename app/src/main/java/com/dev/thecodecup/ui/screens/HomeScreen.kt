package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dev.thecodecup.model.item.CoffeeItem
import com.dev.thecodecup.ui.components.CoffeeCard
import com.dev.thecodecup.ui.components.HomeHeader
import com.dev.thecodecup.ui.components.LoyaltyCard
import com.dev.thecodecup.ui.components.BottomNavBar
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import com.dev.thecodecup.model.db.user.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    coffeeList: List<CoffeeItem>,
    onCoffeeClick: (CoffeeItem) -> Unit,
    onNavClick: (String) -> Unit,
    showEmptyCartDialog: Boolean = false,
    onDismissEmptyCartDialog: () -> Unit = {}
) {
    val user by userViewModel.user.collectAsState()
    val userName = user?.name?.split(" ")?.firstOrNull() ?: "User"
    val userStamps = user?.stamp ?: 0

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (showEmptyCartDialog) {
                AlertDialog(
                    onDismissRequest = onDismissEmptyCartDialog,
                    title = { Text(
                        text = "Cart is empty",
                        fontFamily = poppinsFontFamily
                    ) },
                    text = { Text(text = "Please add something to your cart before checking out.",
                        fontFamily = poppinsFontFamily
                    ) },
                    confirmButton = {
                        Button(
                            onClick = onDismissEmptyCartDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF324A59),
                                contentColor = Color.White
                            ),
                            ) {
                            Text(
                                text = "Confirm",
                                fontFamily = poppinsFontFamily
                            )
                        }
                    }
                )
            }
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                HomeHeader(
                    userName = userName,
                    onNavClick = onNavClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LoyaltyCard(
                        currentStamps = userStamps,
                        maxStamps = 8,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Surface(
                    color = Color(0xFF324A59),
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp,
                                start = 16.dp,
                                end = 16.dp)
                    ) {
                        Text(
                            text = "Choose your coffee",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFD8D8D8),
                                fontFamily = poppinsFontFamily
                            ),
                            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid (
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            items(coffeeList) { coffee ->
                                CoffeeCard(coffee = coffee, onClick = { onCoffeeClick(coffee) })
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .width(350.dp)
            ) {
                BottomNavBar(
                    currentRoute = "home",
                    onItemSelected = onNavClick
                )
            }
        }
    }
}