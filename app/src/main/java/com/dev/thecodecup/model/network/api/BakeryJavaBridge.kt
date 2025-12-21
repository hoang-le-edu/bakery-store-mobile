package com.dev.thecodecup.model.network.api

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dev.thecodecup.model.network.NetworkModule
import kotlinx.coroutines.launch
import retrofit2.Response

// ==== CALLBACK CHO PRODUCT DETAIL / ADD TO CART (như trước) ====

interface ProductDetailCallback {
    fun onResult(response: Response<ProductDetailResponse>?, error: Throwable?)
}

interface AddToCartCallback {
    fun onResult(response: Response<SuccessResponse>?, error: Throwable?)
}

// ==== CALLBACK CHO CART ====

interface CartCallback {
    fun onResult(response: Response<CartResponse>?, error: Throwable?)
}

interface DeleteCartCallback {
    fun onResult(response: Response<SuccessResponse>?, error: Throwable?)
}

interface CreateCartCallback {
    fun onResult(response: Response<SingleCartResponse>?, error: Throwable?)
}

interface RemoveProductCallback {
    fun onResult(response: Response<SuccessResponse>?, error: Throwable?)
}

interface RemoveToppingCallback {
    fun onResult(response: Response<SuccessResponse>?, error: Throwable?)
}

interface UpdateCartProductCallback {
    fun onResult(response: Response<SuccessResponse>?, error: Throwable?)
}

interface CheckoutCallback {
    fun onResult(response: Response<SuccessResponse>?, error: Throwable?)
}

interface PaymentLinkCallback {
    fun onResult(response: Response<PaymentLinkResponse>?, error: Throwable?)
}

interface CustomerOrdersCallback {
    fun onResult(response: Response<CustomerOrdersResponse>?, error: Throwable?)
}

object BakeryJavaBridge {

    private val apiService = NetworkModule.bakeryApiService

    // ---------- PRODUCT DETAIL ----------

    fun loadProductDetail(
        owner: LifecycleOwner,
        productId: String,
        callback: ProductDetailCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.getProductDetail(productId)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun addProductToCart(
        owner: LifecycleOwner,
        request: AddToCartRequest,
        callback: AddToCartCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.addProductToCart(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    // ---------- CART ----------

    fun fetchCart(
        owner: LifecycleOwner,
        callback: CartCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.fetchCart()
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun deleteCart(
        owner: LifecycleOwner,
        orderId: String,
        callback: DeleteCartCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.deleteCart(orderId)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun createCart(
        owner: LifecycleOwner,
        callback: CreateCartCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val request = CreateCartRequest(type = "Online", custom_name = null)
                val response = apiService.createCart(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun removeProductFromCart(
        owner: LifecycleOwner,
        request: RemoveProductFromCartRequest,
        callback: RemoveProductCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.removeProductFromCart(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun removeToppingFromCart(
        owner: LifecycleOwner,
        request: RemoveToppingRequest,
        callback: RemoveToppingCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.removeToppingFromCart(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun updateProductInCart(
        owner: LifecycleOwner,
        request: UpdateCartProductRequest,
        callback: UpdateCartProductCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.updateProductInCart(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun proceedCheckout(
        owner: LifecycleOwner,
        request: CheckoutRequest,
        callback: CheckoutCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.proceedOrder(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun createPaymentLink(
        owner: LifecycleOwner,
        orderId: String,
        callback: PaymentLinkCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val request = CreatePaymentLinkRequest(order_id = orderId)
                val response = apiService.createPaymentLink(request)
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }

    fun loadCustomerOrders(
        owner: LifecycleOwner,
        callback: CustomerOrdersCallback
    ) {
        owner.lifecycleScope.launch {
            try {
                val response = apiService.loadCustomerOrders()
                callback.onResult(response, null)
            } catch (e: Exception) {
                callback.onResult(null, e)
            }
        }
    }
}
