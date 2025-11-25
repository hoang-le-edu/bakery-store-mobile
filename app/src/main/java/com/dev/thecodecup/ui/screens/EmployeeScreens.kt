package com.dev.thecodecup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.thecodecup.ui.components.Header
import com.dev.thecodecup.ui.theme.poppinsFontFamily

@Composable
fun EmployeeDashboardScreen(onBack: () -> Unit = {}, onNavigate: (String) -> Unit = {}) {
    val stats = listOf(
        DashboardStat("Today's orders", "128", Color(0xFF324A59)),
        DashboardStat("Revenue", "$1,560", Color(0xFF8EB69B)),
        DashboardStat("Avg. prep time", "12 min", Color(0xFFEBD4B9))
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Employee", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Today overview",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            stats.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { stat ->
                        DashboardCard(stat = stat, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Quick links",
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickLinkCard(title = "Orders", subtitle = "Confirm & ship", onClick = { onNavigate("employee/orders") })
                QuickLinkCard(title = "Products", subtitle = "Manage catalog", onClick = { onNavigate("employee/products") })
            }
            Spacer(modifier = Modifier.height(12.dp))
            QuickLinkCard(title = "Notifications", subtitle = "New alerts", onClick = { onNavigate("employee/notifications") })
        }
    }
}

@Composable
fun ProductManagementScreen(onBack: () -> Unit = {}) {
    val products = sampleProducts
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Products", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    ManagementRow(
                        title = product.name,
                        subtitle = "Stock: ${product.stock}",
                        trailing = "$${product.price}",
                        status = product.status
                    )
                }
            }
        }
    }
}

@Composable
fun OrderManagementScreen(onBack: () -> Unit = {}) {
    val orders = sampleEmployeeOrders
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Orders", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    ManagementRow(
                        title = "#${order.code}",
                        subtitle = order.customer,
                        trailing = order.total,
                        status = order.status
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeNotificationsScreen(onBack: () -> Unit = {}) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Header(title = "Notifications", onBack = onBack, haveBack = true)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleNotifications) { notification ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFF4F5F7)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = notification.title,
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = notification.body,
                                fontFamily = poppinsFontFamily,
                                color = Color(0xFF7A8A99)
                            )
                            Text(
                                text = notification.time,
                                fontFamily = poppinsFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFFB0B8C3)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(stat: DashboardStat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = stat.color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = stat.label, fontFamily = poppinsFontFamily, color = Color(0xFF7A8A99))
            Text(
                text = stat.value,
                fontFamily = poppinsFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = stat.color
            )
        }
    }
}

@Composable
private fun QuickLinkCard(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.Transparent)
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontFamily = poppinsFontFamily, fontSize = 18.sp)
            Text(text = subtitle, fontFamily = poppinsFontFamily, color = Color(0xFF7A8A99))
        }
    }
}

@Composable
private fun ManagementRow(title: String, subtitle: String, trailing: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = title, fontFamily = poppinsFontFamily, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, fontFamily = poppinsFontFamily, color = Color(0xFF7A8A99))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = trailing, fontFamily = poppinsFontFamily, fontWeight = FontWeight.SemiBold)
                    AssistChip(
                        onClick = {},
                        label = { Text(status) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

private data class DashboardStat(val label: String, val value: String, val color: Color)

private data class ProductRow(val name: String, val stock: Int, val price: Double, val status: String)
private data class EmployeeOrder(val code: String, val customer: String, val total: String, val status: String)
private data class EmployeeNotification(val title: String, val body: String, val time: String)

private val sampleProducts = listOf(
    ProductRow("Cappuccino", 42, 3.5, "Available"),
    ProductRow("Matcha Latte", 18, 4.2, "Low stock"),
    ProductRow("Croissant", 75, 2.5, "Available"),
    ProductRow("Bagel", 20, 2.0, "Restock soon")
)

private val sampleEmployeeOrders = listOf(
    EmployeeOrder("1059", "Nguyen Van A", "$12.40", "Pending"),
    EmployeeOrder("1060", "Tran Thi B", "$24.10", "In progress"),
    EmployeeOrder("1061", "Pham Van C", "$18.60", "Ready"),
    EmployeeOrder("1062", "Do Thi D", "$32.00", "Shipped")
)

private val sampleNotifications = listOf(
    EmployeeNotification("New order", "Order #1063 waiting for confirmation", "2 min ago"),
    EmployeeNotification("Low stock", "Matcha powder below 15%", "12 min ago"),
    EmployeeNotification("Feedback", "Customer rated #1060 with 5★", "1 hr ago")
)
