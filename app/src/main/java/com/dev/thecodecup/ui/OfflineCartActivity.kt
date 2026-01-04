package com.dev.thecodecup.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.thecodecup.R
import com.dev.thecodecup.activity.BaseBottomNavActivity
import com.dev.thecodecup.activity.CheckoutActivity
import com.dev.thecodecup.activity.ProductDetailActivity
import com.dev.thecodecup.adapter.OfflineCartAdapter
import com.dev.thecodecup.model.db.AppDatabase
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.dev.thecodecup.model.db.cart.OfflineCartManager
import com.dev.thecodecup.model.db.cart.SyncStatus
import com.dev.thecodecup.model.network.api.BakeryApiService
import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.services.CartSyncService
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OfflineCartActivity : BaseBottomNavActivity() {
    
    companion object {
        private const val TAG = "OfflineCartActivity"
    }
    
    // Views
    private lateinit var btnHome: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var recyclerViewCartItems: RecyclerView
    private lateinit var emptyCartLayout: View
    private lateinit var checkboxSelectAll: MaterialCheckBox
    private lateinit var txtTotalQuantity: TextView
    private lateinit var txtTotalPrice: TextView
    private lateinit var txtBottomTotalPrice: TextView
    private lateinit var btnProceedToCheckout: MaterialButton
    private lateinit var syncStatusLayout: LinearLayout
    private lateinit var txtSyncStatus: TextView
    private lateinit var txtPendingActions: TextView
    
    // Offline Cart Manager
    private lateinit var offlineCartManager: OfflineCartManager
    private lateinit var cartAdapter: OfflineCartAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_cart)
        
        initViews()
        setupOfflineCartManager()
        setupViews()
        setupListeners()
        observeCartData()
        
        // Start background sync service
        CartSyncService.startSync(this)
    }
    
    override fun getBottomNavMenuItemId(): Int {
        return R.id.navigation_cart
    }
    
    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnRefresh = findViewById(R.id.btnRefresh)
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems)
        emptyCartLayout = findViewById(R.id.emptyCartLayout)
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll)
        txtTotalQuantity = findViewById(R.id.txtTotalQuantity)
        txtTotalPrice = findViewById(R.id.txtTotalPrice)
        txtBottomTotalPrice = findViewById(R.id.txtBottomTotalPrice)
        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout)
        syncStatusLayout = findViewById(R.id.syncStatusLayout)
        txtSyncStatus = findViewById(R.id.txtSyncStatus)
        txtPendingActions = findViewById(R.id.txtPendingActions)
    }
    
    private fun setupOfflineCartManager() {
        val database = AppDatabase.getDatabase(this)
        val apiService = NetworkModule.apiService as BakeryApiService
        
        offlineCartManager = OfflineCartManager(
            context = this,
            cartItemDao = database.cartItemDao(),
            offlineActionDao = database.offlineCartActionDao(),
            bakeryApiService = apiService
        )
    }
    
    private fun setupViews() {
        recyclerViewCartItems.layoutManager = LinearLayoutManager(this)
        
        cartAdapter = OfflineCartAdapter(
            context = this,
            onItemEdit = { item -> editCartItem(item) },
            onQuantityChange = { item, quantity -> updateItemQuantity(item, quantity) },
            onSelectionChanged = { updateSummary() }
        )
        
        recyclerViewCartItems.adapter = cartAdapter
        
        // Setup swipe-to-delete
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView(recyclerViewCartItems)
    }
    
    private fun setupListeners() {
        btnHome.setOnClickListener { finish() }
        
        btnRefresh.setOnClickListener { 
            refreshCart()
        }
        
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cartAdapter.selectAll()
            } else {
                cartAdapter.deselectAll()
            }
        }
        
        btnProceedToCheckout.setOnClickListener {
            proceedToCheckout()
        }
    }
    
    private fun observeCartData() {
        lifecycleScope.launch {
            // Observe cart items
            offlineCartManager.cartItems.collectLatest { items ->
                updateCartDisplay(items)
            }
        }
        
        lifecycleScope.launch {
            // Observe pending actions count
            offlineCartManager.pendingActionsCount.collectLatest { count ->
                updatePendingActionsDisplay(count)
            }
        }
        
        lifecycleScope.launch {
            // Observe sync status
            offlineCartManager.syncStatus.collectLatest { status ->
                updateSyncStatusDisplay(status)
            }
        }
    }
    
    private fun updateCartDisplay(items: List<CartItemEntity>) {
        if (items.isEmpty()) {
            showEmptyCart()
        } else {
            showCartItems(items)
        }
        updateSummary()
    }
    
    private fun showEmptyCart() {
        emptyCartLayout.visibility = View.VISIBLE
        recyclerViewCartItems.visibility = View.GONE
        checkboxSelectAll.visibility = View.GONE
        btnProceedToCheckout.isEnabled = false
    }
    
    private fun showCartItems(items: List<CartItemEntity>) {
        emptyCartLayout.visibility = View.GONE
        recyclerViewCartItems.visibility = View.VISIBLE
        checkboxSelectAll.visibility = View.VISIBLE
        
        cartAdapter.updateItems(items)
        btnProceedToCheckout.isEnabled = items.isNotEmpty()
    }
    
    private fun updatePendingActionsDisplay(count: Int) {
        if (count > 0) {
            txtPendingActions.visibility = View.VISIBLE
            txtPendingActions.text = "Pending sync: $count actions"
        } else {
            txtPendingActions.visibility = View.GONE
        }
    }
    
    private fun updateSyncStatusDisplay(status: SyncStatus) {
        when (status) {
            is SyncStatus.Idle -> {
                txtSyncStatus.text = "Ready"
                txtSyncStatus.setTextColor(getColor(android.R.color.darker_gray))
            }
            is SyncStatus.Syncing -> {
                txtSyncStatus.text = "Syncing..."
                txtSyncStatus.setTextColor(getColor(android.R.color.holo_blue_dark))
            }
            is SyncStatus.Success -> {
                txtSyncStatus.text = "Synced"
                txtSyncStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            }
            is SyncStatus.Error -> {
                txtSyncStatus.text = "Error: ${status.message}"
                txtSyncStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        }
    }
    
    private fun updateSummary() {
        val selectedItems = cartAdapter.getSelectedItems()
        val totalQuantity = selectedItems.sumOf { it.quantity }
        val totalPrice = selectedItems.sumOf { it.totalPrice }
        
        txtTotalQuantity.text = "Total: $totalQuantity items"
        txtTotalPrice.text = "$${String.format("%.2f", totalPrice)}"
        txtBottomTotalPrice.text = "$${String.format("%.2f", totalPrice)}"
        
        updateSelectAllCheckbox()
    }
    
    private fun updateSelectAllCheckbox() {
        checkboxSelectAll.setOnCheckedChangeListener(null)
        checkboxSelectAll.isChecked = cartAdapter.isAllSelected()
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cartAdapter.selectAll()
            } else {
                cartAdapter.deselectAll()
            }
        }
    }
    
    private fun editCartItem(item: CartItemEntity) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("PRODUCT_ID", item.productId)
        intent.putExtra("EDIT_MODE", true)
        intent.putExtra("CART_ITEM_ID", item.id)
        intent.putExtra("CURRENT_QUANTITY", item.quantity)
        startActivity(intent)
    }
    
    private fun updateItemQuantity(item: CartItemEntity, quantity: Int) {
        lifecycleScope.launch {
            if (quantity <= 0) {
                removeCartItem(item)
            } else {
                val success = offlineCartManager.updateItemQuantity(item.id, quantity)
                if (!success) {
                    showError("Failed to update item quantity")
                }
            }
        }
    }
    
    private fun removeCartItem(item: CartItemEntity) {
        AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove ${item.productName} from cart?")
            .setPositiveButton("Remove") { _, _ ->
                lifecycleScope.launch {
                    val success = offlineCartManager.removeItem(item.id)
                    if (!success) {
                        showError("Failed to remove item")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun refreshCart() {
        lifecycleScope.launch {
            val success = offlineCartManager.refreshCartFromServer()
            if (!success) {
                showError("Failed to refresh cart")
            } else {
                showSuccess("Cart refreshed")
            }
        }
    }
    
    private fun proceedToCheckout() {
        val selectedItems = cartAdapter.getSelectedItems()
        
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select items to checkout", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if there are pending sync actions
        if (offlineCartManager.pendingActionsCount.value > 0) {
            AlertDialog.Builder(this)
                .setTitle("Pending Changes")
                .setMessage("You have ${offlineCartManager.pendingActionsCount.value} pending changes that haven't synced yet. Do you want to proceed with checkout?")
                .setPositiveButton("Proceed") { _, _ -> 
                    proceedWithCheckout(selectedItems)
                }
                .setNegativeButton("Wait for Sync", null)
                .show()
        } else {
            proceedWithCheckout(selectedItems)
        }
    }
    
    private fun proceedWithCheckout(selectedItems: List<CartItemEntity>) {
        val selectedIds = ArrayList(selectedItems.map { it.id })
        val totalPrice = selectedItems.sumOf { it.totalPrice }
        
        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putIntegerArrayListExtra("CART_ITEM_IDS", selectedIds)
        intent.putExtra("ORDER_TOTAL", totalPrice)
        startActivity(intent)
    }
    
    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(android.R.color.holo_red_dark))
            .show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(android.R.color.holo_green_dark))
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        offlineCartManager.cleanup()
    }
    
    // Swipe to delete implementation
    private inner class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val item = cartAdapter.getItemAtPosition(position)
            removeCartItem(item)
        }
    }
}