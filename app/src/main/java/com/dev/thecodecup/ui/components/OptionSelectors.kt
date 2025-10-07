package com.dev.thecodecup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.R
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun QuantitySelector(
    title: String,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            fontFamily = poppinsFontFamily,
            modifier = Modifier.padding(end = 16.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFFD8D8D8),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }) {
                Text("-", fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily)
            }
            Text(
                quantity.toString(),
                fontSize = 16.sp,
                fontFamily = poppinsFontFamily,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = poppinsFontFamily)
            }
        }
    }
}

@Composable
fun TextOptionSelector(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            fontFamily = poppinsFontFamily,
            modifier = Modifier.padding(end = 16.dp)
        )
        Row {
            options.forEach { option ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (selected == option) Color(0xFF324A59)
                    else Color(0xFFF7F8FB),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { onSelect(option) }
                        .border(
                            width = 1.dp,
                            color = if (selected == option) Color(0xFF324A59)
                            else Color(0xFFD8D8D8),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(
                                width = 100.dp,
                                height = 40.dp
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            option,
                            color = if (selected == option) Color(0xFFD8D8D8) else Color(0xFF324A59),
                            fontWeight = FontWeight.Medium,
                            fontFamily = poppinsFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageOptionSelector(
    modifier: Modifier = Modifier,
    title: String,
    options: List<Pair<String, Int>>,
    selected: String,
    onSelect: (String) -> Unit,
    isSize : Boolean = false,
    isIce : Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            fontFamily = poppinsFontFamily,
            modifier = Modifier.padding(end = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (option, imageRes) ->
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = option,
                        modifier = Modifier
                            .size(
                                if(isSize) {
                                    when (option) {
                                        "Small" -> 30.dp
                                        "Medium" -> 40.dp
                                        else -> 50.dp
                                    }
                                } else if(isIce) {
                                    when (option) {
                                        "Less" -> 25.dp
                                        else -> 40.dp
                                    }
                                } else 40.dp
                            )
                            .clickable { onSelect(option) },
                        colorFilter = if(selected == option) ColorFilter.tint(Color(0xFF001833))
                        else ColorFilter.tint(Color(0xFFD8D8D8))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQuantitySelector() {
    var quantity by remember { mutableIntStateOf(2) }
    QuantitySelector(title = "Quantity", quantity = quantity, onQuantityChange = { quantity = it })
}

@Preview(showBackground = true)
@Composable
fun PreviewTextOptionSelector() {
    var selected by remember { mutableStateOf("Single") }
    TextOptionSelector(
        title = "Serving",
        options = listOf("Single", "Double"),
        selected = selected,
        onSelect = { selected = it }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewImageOptionSelectorTakeaway() {
    var selected by remember { mutableStateOf("Here") }
    ImageOptionSelector(
        title = "Select",
        options = listOf(
            "Here" to R.drawable.ic_stay,
            "Takeaway" to R.drawable.ic_takeaway
        ),
        selected = selected,
        onSelect = { selected = it }
    )
}