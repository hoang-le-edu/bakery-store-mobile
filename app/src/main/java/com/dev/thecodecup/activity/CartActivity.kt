package com.dev.thecodecup.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.thecodecup.R
import com.dev.thecodecup.adapter.CartAdapter
import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.model.network.api.Cart
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity() {
    
    private val apiService = NetworkModule.bakeryApiService
    
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerViewCartItems: RecyclerView
    private lateinit var emptyCartLayout: View
    private lateinit var txtTotalPrice: TextView
    private lateinit var txtBottomTotalPrice: TextView
    private lateinit var btnDeleteCart: Button
    private lateinit var btnProceedToCheckout: Button
    
    private var carts = listOf<Cart>()
    private var selectedCartIndex = 0
    private lateinit var cartAdapter: CartAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        
        initViews()
        setupViews()
        setupListeners()
        loadCarts()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems)
        emptyCartLayout = findViewById(R.id.emptyCartLayout)
        txtTotalPrice = findViewById(R.id.txtTotalPrice)
        txtBottomTotalPrice = findViewById(R.id.txtBottomTotalPrice)
        btnDeleteCart = findViewById(R.id.btnDeleteCart)
        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout)
    }
    
    private fun setupViews() {
        recyclerViewCartItems.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(this) { item ->
            // Handle item click if needed
        }
        recyclerViewCartItems.adapter = cartAdapter
    }
    
    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedCartIndex = tab.position
                displayCartItems()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        
        btnProceedToCheckout.setOnClickListener {
            if (carts.isNotEmpty() && selectedCartIndex < carts.size) {
                val selectedCart = carts[selectedCartIndex]
                // TODO: Navigate to CheckoutActivity when it's ready
                Toast.makeText(this, "Checkout chưa được implement", Toast.LENGTH_SHORT).show()
                // val intent = Intent(this, CheckoutActivity::class.java)
                // intent.putExtra("CART_ORDER_ID", selectedCart.order_id)
                // intent.putExtra("CART_TOTAL", selectedCart.total_price)
                // startActivity(intent)
            }
        }
        
        btnDeleteCart.setOnClickListener {
            deleteCurrentCart()
        }
    }
    
    private fun loadCarts() {
        val dialog = ProgressDialog.show(this, null, "Đang tải giỏ hàng...", true, false)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.fetchCart()
                }
                
                dialog.dismiss()
                
                if (response.isSuccessful && response.body() != null) {
                    carts = response.body()!!.data
                    if (carts.isNotEmpty()) {
                        setupCartTabs()
                        displayCartItems()
                    } else {
                        showEmptyCart()
                    }
                } else {
                    Toast.makeText(this@CartActivity, "Lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                dialog.dismiss()
                Toast.makeText(this@CartActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupCartTabs() {
        tabLayout.removeAllTabs()
        
        carts.forEachIndexed { index, cart ->
            val tab = tabLayout.newTab()
            tab.text = "${cart.name} (${cart.count_product})"
            tabLayout.addTab(tab)
        }
        
        if (tabLayout.tabCount > 0) {
            tabLayout.getTabAt(0)?.select()
        }
    }
    
    private fun displayCartItems() {
        if (carts.isEmpty() || selectedCartIndex >= carts.size) {
            showEmptyCart()
            return
        }
        
        val selectedCart = carts[selectedCartIndex]
        
        if (selectedCart.order_detail.isEmpty()) {
            showEmptyCart()
            return
        }
        
        emptyCartLayout.visibility = View.GONE
        recyclerViewCartItems.visibility = View.VISIBLE
        btnProceedToCheckout.visibility = View.VISIBLE
        btnDeleteCart.visibility = View.VISIBLE
        
        cartAdapter.setItems(selectedCart.order_detail)
        txtTotalPrice.text = formatPrice(selectedCart.total_price) + "₫"
        txtBottomTotalPrice.text = formatPrice(selectedCart.total_price) + "₫"
    }
    
    private fun showEmptyCart() {
        emptyCartLayout.visibility = View.VISIBLE
        recyclerViewCartItems.visibility = View.GONE
        btnProceedToCheckout.visibility = View.GONE
        btnDeleteCart.visibility = View.GONE
        txtTotalPrice.text = "0₫"
        txtBottomTotalPrice.text = "0₫"
    }
    
    private fun deleteCurrentCart() {
        if (carts.isEmpty() || selectedCartIndex >= carts.size) return
        
        val selectedCart = carts[selectedCartIndex]
        
        val dialog = ProgressDialog.show(this, null, "Đang xóa giỏ hàng...", true, false)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.deleteCart(selectedCart.order_id)
                }
                
                dialog.dismiss()
                
                if (response.isSuccessful) {
                    Toast.makeText(this@CartActivity, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show()
                    loadCarts() // Reload
                } else {
                    Toast.makeText(this@CartActivity, "Lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                dialog.dismiss()
                Toast.makeText(this@CartActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun formatPrice(price: Int): String {
        return String.format("%,d", price).replace(",", ".")
    }
    
    override fun onResume() {
        super.onResume()
        loadCarts() // Refresh cart when returning to this screen
    }
}
