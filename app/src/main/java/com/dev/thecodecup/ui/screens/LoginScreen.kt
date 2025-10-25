package com.dev.thecodecup.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.R
import com.dev.thecodecup.auth.AuthViewModel
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    
    // Activity result launcher for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle sign-in result (even if user cancels)
        authViewModel.handleSignInResult(result.data)
    }
    
    // Navigate to home when login successful
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            onLoginSuccess()
        }
    }
    
    // Show error dialog
    if (authState.error != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            title = { Text("Sign In Error", fontFamily = poppinsFontFamily) },
            text = { Text(authState.error ?: "", fontFamily = poppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearError() }) {
                    Text("OK", fontFamily = poppinsFontFamily)
                }
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Icon
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome Text
            Text(
                text = "Welcome to",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = poppinsFontFamily,
                color = Color(0xFF324A59)
            )
            
            Text(
                text = "The Code Cup",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                color = Color(0xFF324A59)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                fontFamily = poppinsFontFamily,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Google Sign-In Button
            Button(
                onClick = {
                    val signInIntent = authViewModel.getSignInIntent()
                    launcher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                ),
                enabled = !authState.isLoading
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF324A59)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Signing in...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = poppinsFontFamily
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = poppinsFontFamily
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Terms and Privacy
            Text(
                text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                fontSize = 12.sp,
                fontFamily = poppinsFontFamily,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

