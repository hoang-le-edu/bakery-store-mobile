package com.dev.thecodecup.ui.screens

import com.dev.thecodecup.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
             painter = painterResource(id = R.drawable.background_splash_screen),
            contentDescription = "Splash Background",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .zIndex(2f)
        )

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .offset(y = (-64).dp)
                .zIndex(4f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(160.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.splash_screen_name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFFFAF6),
                fontFamily = poppinsFontFamily
            )
        }
    }
}

@Composable
@Preview
fun SplashScreenPreview() {
    SplashScreen(onSplashFinished = {})
}