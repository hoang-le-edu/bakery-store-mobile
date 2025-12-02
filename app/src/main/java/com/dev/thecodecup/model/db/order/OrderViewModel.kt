package com.dev.thecodecup.model.db.order

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OrderRepository.getInstance(application)

    private val _ongoingOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val ongoingOrders: StateFlow<List<OrderEntity>> = _ongoingOrders.asStateFlow()

    private val _historyOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val historyOrders: StateFlow<List<OrderEntity>> = _historyOrders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderEntity?>(null)
    val selectedOrder: StateFlow<OrderEntity?> = _selectedOrder.asStateFlow()

    init {
        loadOngoingOrders()
        loadHistoryOrders()
    }

    fun loadOngoingOrders() {
        viewModelScope.launch {
            _ongoingOrders.value = repository.getOngoingOrders()
        }
    }

    fun loadHistoryOrders() {
        viewModelScope.launch {
            _historyOrders.value = repository.getHistoryOrders()
        }
    }

    fun insertOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.insertOrder(order)
            if (order.isHistory) loadHistoryOrders() else loadOngoingOrders()
        }
    }


    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            if (order.isHistory) loadHistoryOrders() else loadOngoingOrders()
        }
    }

    fun updateOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.updateOrder(order)
            loadOngoingOrders()
            loadHistoryOrders()
        }
    }

    fun loadOrderById(orderId: Int) {
        viewModelScope.launch {
            _selectedOrder.value = repository.getOrderById(orderId)
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

}
