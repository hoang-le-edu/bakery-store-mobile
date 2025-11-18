package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.ui.components.ProfileCard
import com.dev.thecodecup.R
import com.dev.thecodecup.auth.AuthViewModel
import com.dev.thecodecup.model.db.user.UserViewModel
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel? = null,
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onEmployeeHubClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {}
) {
    val user = userViewModel.user.collectAsState().value
    var editingField by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        name = user?.name ?: ""
        email = user?.email ?: ""
        phone = user?.phone ?: ""
        address = user?.address ?: ""
    }

    fun saveUser() {
        user?.let {
            val updatedUser = it.copy(
                name = name,
                email = email,
                phone = phone,
                address = address
            )
            userViewModel.updateUser(updatedUser)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
                top = 50.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(
            title = "Profile",
            onBack = onBack,
            haveBack = true
        )
        ProfileCard(
            label = "Full name",
            value = name,
            painterResId = R.drawable.ic_profile,
            isEditing = editingField == "name",
            onEditClick = {
                if (editingField == "name") {
                    if (name != user?.name) saveUser()
                    editingField = ""
                } else {
                    editingField = "name"
                }
            },
            onValueChange = { name = it }
        )
        ProfileCard(
            label = "Phone number",
            value = phone,
            painterResId = R.drawable.ic_phone,
            isEditing = editingField == "phone",
            onEditClick = {
                if (editingField == "phone") {
                    if (phone != user?.phone) saveUser()
                    editingField = ""
                } else {
                    editingField = "phone"
                }
            },
            onValueChange = { phone = it }
        )
        ProfileCard(
            label = "Email",
            value = email,
            painterResId = R.drawable.ic_email,
            isEditing = editingField == "email",
            onEditClick = {
                if (editingField == "email") {
                    if (email != user?.email) saveUser()
                    editingField = ""
                } else {
                    editingField = "email"
                }
            },
            onValueChange = { email = it }
        )
        ProfileCard(
            label = "Address",
            value = address,
            painterResId = R.drawable.ic_location,
            isEditing = editingField == "address",
            onEditClick = {
                if (editingField == "address") {
                    if (address != user?.address) saveUser()
                    editingField = ""
                } else {
                    editingField = "address"
                }
            },
            onValueChange = { address = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick actions",
            fontFamily = poppinsFontFamily,
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        ActionGrid(
            actions = listOf(
                ProfileAction("Settings", Icons.Outlined.Settings, onSettingsClick),
                ProfileAction("Notifications", Icons.Outlined.Notifications, onNotificationClick),
                ProfileAction("Chat", Icons.Outlined.Chat, onChatClick),
                ProfileAction("Employee", Icons.Outlined.Work, onEmployeeHubClick),
                ProfileAction("Language", Icons.Outlined.Language, onLanguageClick)
            )
        )

        // Sign Out Button (if AuthViewModel is provided)
        if (authViewModel != null) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    authViewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE74C3C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Sign Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = poppinsFontFamily
                )
            }
        }
    }
}

private data class ProfileAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun ActionGrid(actions: List<ProfileAction>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { action ->
                    ActionCard(action = action, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ActionCard(action: ProfileAction, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF4F5F7),
        tonalElevation = 2.dp,
        onClick = action.onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = Color(0xFF324A59)
                    )
                }
            }
            Text(
                text = action.label,
                fontFamily = poppinsFontFamily,
                fontSize = 16.sp
            )
        }
    }
}