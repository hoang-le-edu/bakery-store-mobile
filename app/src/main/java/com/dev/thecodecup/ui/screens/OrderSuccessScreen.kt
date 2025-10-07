package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.dev.thecodecup.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun OrderSuccessScreen(
    onTrackOrder: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.order_success),
                    contentDescription = "Order Success",
                    modifier = Modifier
                        .size(200.dp)
                )
                Text(
                    text = "Order Success",
                    modifier = Modifier.padding(top = 16.dp),
                    fontFamily = poppinsFontFamily,
                    color = Color(0xFF181D2D),
                    fontSize = 24.sp
                )
                Text(
                    text = "Your order has been placed successfully.",
                    modifier = Modifier.padding(top = 16.dp),
                    fontFamily = poppinsFontFamily,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
                Text(
                    text = "For more details, go to my orders.",
                    fontFamily = poppinsFontFamily,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
                Button(
                    onClick = { onTrackOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 100   .dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                        .height(50.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF324A59),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Track My Order",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppinsFontFamily
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun OrderSuccessScreenPreview() {
    OrderSuccessScreen(onTrackOrder = {})
}