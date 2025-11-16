package com.dev.thecodecup.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.thecodecup.model.db.order.OrderEntity
import com.dev.thecodecup.model.db.order.OrderViewModel

@Composable
fun HistoryList(
    orderViewModel: OrderViewModel,
    onOrderSelected: (OrderEntity) -> Unit = {}
) {
    val historyOrderList = orderViewModel.historyOrders.collectAsState().value
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(historyOrderList.size) {
            Spacer(modifier = Modifier.height(8.dp))
            val order = historyOrderList[it]
            OrderItemCard(
                orderItem = order,
                isHistoryCard = true,
                onClick = { onOrderSelected(order) }
            )
        }
    }
}