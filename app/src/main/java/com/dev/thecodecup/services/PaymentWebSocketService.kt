package com.dev.thecodecup.services

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Interface for Java compatibility to listen to payment updates
 */
interface PaymentUpdateListener {
    fun onPaymentSuccess(orderId: String, amount: String, orderNumber: String)
    fun onPaymentFailed(reason: String)
    fun onPaymentError(message: String)
    fun onConnectionStatusChanged(connected: Boolean, message: String)
}

/**
 * WebSocket service for listening to PayOS payment status updates
 * This service can be shared across multiple activities
 */
class PaymentWebSocketService(private val context: Context) {

    companion object {
        private const val TAG = "PaymentWebSocketService"
        private const val SOCKET_URL = "https://socket.dotb.cloud/"

        @Volatile
        private var INSTANCE: PaymentWebSocketService? = null

        @JvmStatic
        fun getInstance(context: Context): PaymentWebSocketService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PaymentWebSocketService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Java-friendly callback listeners
    private val listeners: MutableSet<PaymentUpdateListener> = mutableSetOf()

    // Flow for payment status updates
    private val _paymentUpdates = MutableSharedFlow<PaymentUpdate>()
    val paymentUpdates: SharedFlow<PaymentUpdate> = _paymentUpdates.asSharedFlow()

    // Flow for connection status
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    /**
     * Add payment update listener for Java compatibility
     */
    fun addPaymentUpdateListener(listener: PaymentUpdateListener) {
        listeners.add(listener)
    }

    /**
     * Remove payment update listener
     */
    fun removePaymentUpdateListener(listener: PaymentUpdateListener) {
        listeners.remove(listener)
    }

    /**
     * Connect to WebSocket server
     */
    fun connect() {
        if (socket?.connected() == true) {
            Log.d(TAG, "Already connected to WebSocket")
            return
        }

        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 5
                timeout = 10000
            }

            socket = IO.socket(SOCKET_URL, options)

            setupEventListeners()
            socket?.connect()
            Log.d(TAG, "Connecting to PayOS WebSocket...")

        } catch (e: URISyntaxException) {
            Log.e(TAG, "WebSocket URI error", e)
            _connectionStatus.value = ConnectionStatus.Error("URI Error: ${e.message}")
        }
    }

    /**
     * Subscribe to payment updates for a specific order
     */
    fun subscribeToOrderPayment(orderId: String) {
        val room = "triggerPaymentStatus/$orderId"
        socket?.emit("join", room)
        Log.d(TAG, "Subscribed to payment updates for order: $orderId (room: $room)")
    }

    /**
     * Unsubscribe from payment updates for a specific order
     */
    fun unsubscribeFromOrderPayment(orderId: String) {
        val room = "triggerPaymentStatus/$orderId"
        socket?.emit("leave", room)
        Log.d(TAG, "Unsubscribed from payment updates for order: $orderId")
    }

    /**
     * Setup event listeners for WebSocket
     */
    private fun setupEventListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to PayOS WebSocket server")
                _connectionStatus.value = ConnectionStatus.Connected
                // Notify Java listeners
                listeners.forEach { it.onConnectionStatusChanged(true, "Connected") }
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args[0] as? Exception
                Log.e(TAG, "WebSocket connection error", error)
                val errorMsg = "Connection error: ${error?.message}"
                _connectionStatus.value = ConnectionStatus.Error(errorMsg)
                // Notify Java listeners
                listeners.forEach { it.onConnectionStatusChanged(false, errorMsg) }
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                Log.w(TAG, "WebSocket disconnected: ${args[0]}")
                _connectionStatus.value = ConnectionStatus.Disconnected
                // Notify Java listeners
                listeners.forEach { it.onConnectionStatusChanged(false, "Disconnected") }
            }

            // Using the string literal "reconnect" which is widely supported
            on("reconnect") {
                Log.d(TAG, "WebSocket reconnected")
                _connectionStatus.value = ConnectionStatus.Connected
                // Notify Java listeners
                listeners.forEach { it.onConnectionStatusChanged(true, "Reconnected") }
            }

            on("event-phenikaa") { args ->
                scope.launch {
                    try {
                        val msg = args[0] as JSONObject
                        val paymentUpdate = parsePaymentUpdate(msg)
                        _paymentUpdates.emit(paymentUpdate)
                        Log.d(TAG, "Payment update emitted: $paymentUpdate")

                        // Notify Java listeners
                        when (paymentUpdate) {
                            is PaymentUpdate.Success -> {
                                listeners.forEach {
                                    it.onPaymentSuccess(
                                        paymentUpdate.orderId,
                                        paymentUpdate.amount,
                                        paymentUpdate.orderNumber
                                    )
                                }
                            }
                            is PaymentUpdate.Failed -> {
                                listeners.forEach { it.onPaymentFailed(paymentUpdate.reason) }
                            }
                            is PaymentUpdate.Error -> {
                                listeners.forEach { it.onPaymentError(paymentUpdate.message) }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing payment update", e)
                        val errorMsg = "Parse error: ${e.message}"
                        _paymentUpdates.emit(PaymentUpdate.Error(errorMsg))
                        listeners.forEach { it.onPaymentError(errorMsg) }
                    }
                }
            }
        }
    }

    /**
     * Parse PayOS webhook data into PaymentUpdate object
     */
    private fun parsePaymentUpdate(msg: JSONObject): PaymentUpdate {
        return try {
            val success = msg.optBoolean("success", false)

            if (success) {
                val data = msg.optJSONObject("data")
                if (data != null) {
                    PaymentUpdate.Success(
                        orderId = data.optString("order_id", ""),
                        amount = data.optString("amount", ""),
                        orderNumber = data.optString("order_number", ""),
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    PaymentUpdate.Success(
                        orderId = "",
                        amount = "",
                        orderNumber = "",
                        timestamp = System.currentTimeMillis()
                    )
                }
            } else {
                PaymentUpdate.Failed("Payment failed")
            }
        } catch (e: Exception) {
            PaymentUpdate.Error("Parse error: ${e.message}")
        }
    }

    /**
     * Disconnect from WebSocket
     */
    fun disconnect() {
        socket?.apply {
            disconnect()
            off()
        }
        socket = null
        _connectionStatus.value = ConnectionStatus.Disconnected
        Log.d(TAG, "Disconnected from PayOS WebSocket")
    }

    /**
     * Check if WebSocket is connected
     */
    fun isConnected(): Boolean = socket?.connected() == true
}

/**
 * Sealed class for payment update events
 */
sealed class PaymentUpdate {
    data class Success(
        val orderId: String,
        val amount: String,
        val orderNumber: String,
        val timestamp: Long
    ) : PaymentUpdate()

    data class Failed(val reason: String) : PaymentUpdate()
    data class Error(val message: String) : PaymentUpdate()
}

/**
 * Connection status enum
 */
sealed class ConnectionStatus {
    object Connected : ConnectionStatus()
    object Disconnected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}
