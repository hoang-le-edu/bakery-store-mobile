package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.dev.thecodecup.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.thecodecup.ui.theme.poppinsFontFamily
import androidx.compose.runtime.setValue

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    painterResId: Int,
    isEditing: Boolean = false,
    onEditClick: () -> Unit = {},
    onValueChange: (String) -> Unit = {}
) {
    var localValue by remember { mutableStateOf(value) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = Color(0xFFEDEFF5),
                        shape = CircleShape
                    )
            ) {
                Image(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .size(20.dp)
                        .align(Alignment.Center),
                    painter = painterResource(painterResId),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp
                    )
                    .weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF001833).copy(alpha = 0.22f),
                    fontFamily = poppinsFontFamily
                )
                if (isEditing) {
                    OutlinedTextField(
                        value = localValue,
                        onValueChange = {
                            localValue = it
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF324A59),
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .fillMaxHeight()
                    .clickable { onEditClick() },
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "Edit"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileCardPreview() {
    ProfileCard(
        label = "Full name",
        value = "Khoa Ho",
        painterResId = R.drawable.ic_profile,
    )
}