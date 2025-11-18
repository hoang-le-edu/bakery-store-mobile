package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onLanguageClick: () -> Unit = {}
) {
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var marketingEmails by rememberSaveable { mutableStateOf(false) }
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Settings", onBack = onBack, haveBack = true)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Notifications",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingToggleRow(
                title = "Push notifications",
                description = "Receive order status updates",
                isChecked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            SettingToggleRow(
                title = "Promotions",
                description = "Weekly deals and coupons",
                isChecked = marketingEmails,
                onCheckedChange = { marketingEmails = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Appearance",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF4F5F7)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark mode",
                            fontFamily = poppinsFontFamily,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (darkTheme) "Enabled" else "Disabled",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFF7A8A99)
                        )
                    }
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { darkTheme = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Localization",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF4F5F7)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Language & region",
                            fontFamily = poppinsFontFamily,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "English (default)",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFF7A8A99)
                        )
                    }
                    IconButton(onClick = onLanguageClick) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = "Language",
                            tint = Color(0xFF324A59)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Privacy",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF4F5F7)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyRow(title = "Download data")
                    PrivacyRow(title = "Clear search history")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* future hook */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF324A59),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save preferences",
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF4F5F7)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp
                )
                Text(
                    text = description,
                    fontFamily = poppinsFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF7A8A99)
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun PrivacyRow(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = poppinsFontFamily,
            fontSize = 15.sp
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = title,
            modifier = Modifier.size(18.dp),
            tint = Color(0xFF324A59)
        )
    }
}
