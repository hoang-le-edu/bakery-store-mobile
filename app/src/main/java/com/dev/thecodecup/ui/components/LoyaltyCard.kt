package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dev.thecodecup.R
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun LoyaltyCard(
    modifier: Modifier = Modifier,
    currentStamps: Int,
    maxStamps: Int = 8,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF324A59))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Loyalty card",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD8D8D8),
                    fontFamily = poppinsFontFamily
                )
                Text(
                    text = "$currentStamps / $maxStamps",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD8D8D8),
                    fontFamily = poppinsFontFamily
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(vertical = 23.25.dp, horizontal = 5.1.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    repeat(maxStamps) { index ->
                        Image(
                            painter = if(index < currentStamps) painterResource(id = R.drawable.fullfilled_loyalty)
                            else
                                painterResource(id = R.drawable.unfullfilled_loyalty),
                            contentDescription = "Stamp ${index + 1}",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
