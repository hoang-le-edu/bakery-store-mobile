package com.dev.thecodecup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import com.dev.thecodecup.model.item.BottomNavItem
import com.dev.thecodecup.model.data.bottomNavItems

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = bottomNavItems,
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(76.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onItemSelected(item.route) }
                ) {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.route,
                        modifier = Modifier
                            .size(
                                if (isSelected) 34.dp
                                else 28.dp),
                        colorFilter = ColorFilter.tint(
                            if (isSelected) Color(0xFF324A59)
                            else Color(0xFFD8D8D8)
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {

    BottomNavBar(
        currentRoute = "home",
        onItemSelected = {}
    )
}