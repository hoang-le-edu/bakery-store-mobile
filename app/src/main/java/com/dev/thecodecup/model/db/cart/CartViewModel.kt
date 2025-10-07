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
        return _cartItems.value.sumOf {
                it.quantity * when (it.size) {
                CoffeeSize.SMALL -> 0.9
                CoffeeSize.MEDIUM -> 1.0
                CoffeeSize.LARGE -> 1.1
            } * when (it.shot) {
                ShotLevel.SINGLE -> 1.0
                ShotLevel.DOUBLE -> 1.5
            } * it.coffee.price
        }
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

