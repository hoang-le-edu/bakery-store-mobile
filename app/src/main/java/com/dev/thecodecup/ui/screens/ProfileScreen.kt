package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.dev.thecodecup.ui.components.ProfileCard
import com.dev.thecodecup.R
import com.dev.thecodecup.model.db.user.UserViewModel
import com.dev.thecodecup.ui.components.Header

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit = {}
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
    }
}