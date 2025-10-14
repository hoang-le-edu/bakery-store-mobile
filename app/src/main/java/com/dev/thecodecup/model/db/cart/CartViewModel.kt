package com.dev.thecodecup.model.db.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CartRepository.getInstance(application)

    private val _cartItems = MutableStateFlow<List<CartItemEntity>>(emptyList())
    val cartItems: StateFlow<List<CartItemEntity>> = _cartItems.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _cartItems.value = repository.getAllCartItems()
        }
    }

    fun getCartPrice(): Double {
        return _cartItems.value.sumOf { it.price * it.quantity }
    }
    fun addCartItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.insertCartItem(item)
            loadCart()
        }
    }

    fun removeCartItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.deleteCartItem(item)
            loadCart()
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            loadCart()
        }
    }
}

