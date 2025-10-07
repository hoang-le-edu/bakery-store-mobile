package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.thecodecup.R
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun Header(
    title: String,
    onBack: () -> Unit = {},
    onCart: () -> Unit = {},
    haveCart: Boolean = false,
    haveBack: Boolean = false
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if(haveBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_arrowback),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Center),
            fontFamily = poppinsFontFamily,
            color = Color(0xFF001833)
        )
        if(haveCart) {
            IconButton(
                onClick = onCart,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Image(
                    painter = painterResource(R.drawable.buy),
                    contentDescription = "Cart",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewHeader() {
    Header(
        title = "Coffee Detail",
        onBack = {},
        onCart = {},
        haveCart = true,
        haveBack = true
    )
}
