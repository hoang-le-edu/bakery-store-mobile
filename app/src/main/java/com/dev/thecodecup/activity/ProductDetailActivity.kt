package com.dev.thecodecup.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.dev.thecodecup.R
import com.dev.thecodecup.adapter.ImageCarouselAdapter
import com.dev.thecodecup.model.network.NetworkModule
import com.dev.thecodecup.model.network.api.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailActivity : AppCompatActivity() {
    
    private val apiService = NetworkModule.bakeryApiService
    
    private lateinit var imageCarousel: ViewPager2
    private lateinit var btnBack: ImageButton
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var chipGroupSizes: ChipGroup
    private lateinit var chipGroupToppings: ChipGroup
    private lateinit var btnDecrease: ImageButton
    private lateinit var btnIncrease: ImageButton
    private lateinit var tvQuantity: TextView
    private lateinit var etNote: EditText
    private lateinit var btnAddToCart: Button
    
    private var productDetail: ProductDetail? = null
    private var productId: String = ""
    private var selectedSize: String? = null
    private var selectedSizePrice: Int = 0
    private val selectedToppings = mutableListOf<Topping>()
    private var quantity = 1
    private var hasSize: Boolean = false
    
    private val carouselHandler = Handler(Looper.getMainLooper())
    private var currentImageIndex = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        
        productId = intent.getStringExtra("PRODUCT_ID") ?: run {
            Toast.makeText(this, "Product ID không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        setupListeners()
        loadProductDetail()
    }
    
    private fun initViews() {
        imageCarousel = findViewById(R.id.imageCarousel)
        btnBack = findViewById(R.id.btnBack)
        tvProductName = findViewById(R.id.tvProductName)
        tvProductPrice = findViewById(R.id.tvProductPrice)
        chipGroupSizes = findViewById(R.id.chipGroupSizes)
        chipGroupToppings = findViewById(R.id.chipGroupToppings)
        btnDecrease = findViewById(R.id.btnDecrease)
        btnIncrease = findViewById(R.id.btnIncrease)
        tvQuantity = findViewById(R.id.tvQuantity)
        etNote = findViewById(R.id.etNote)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
    }
    
    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        
        btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityAndPrice()
            }
        }
        
        btnIncrease.setOnClickListener {
            quantity++
            updateQuantityAndPrice()
        }
        
        btnAddToCart.setOnClickListener { addToCart() }
    }
    
    private fun loadProductDetail() {
        val dialog = ProgressDialog.show(this, null, "Đang tải...", true, false)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getProductDetail(productId)
                }
                
                dialog.dismiss()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    productDetail = response.body()?.data
                    if (productDetail != null) {
                        displayProductDetail()
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Dữ liệu sản phẩm null", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMsg = "API Error: ${response.code()} - ${response.message()}\nProduct ID: $productId"
                    Toast.makeText(this@ProductDetailActivity, errorMsg, Toast.LENGTH_LONG).show()
                    android.util.Log.e("ProductDetail", "API failed: $errorMsg")
                    android.util.Log.e("ProductDetail", "Response body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                dialog.dismiss()
                val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}\nProduct ID: $productId"
                Toast.makeText(this@ProductDetailActivity, errorMsg, Toast.LENGTH_LONG).show()
                android.util.Log.e("ProductDetail", "Exception loading product", e)
            }
        }
    }
    
    private fun displayProductDetail() {
        val product = productDetail ?: return
        
        tvProductName.text = product.name
        tvProductPrice.text = formatPrice(product.price) + "₫"
        
        // Setup image carousel
        val imageUrls = mutableListOf<String>()
        product.image_url?.let { imageUrls.add(it) }
        product.productDetailImages?.forEach { imageUrls.add(it.image_url) }
        
        if (imageUrls.isNotEmpty()) {
            val adapter = ImageCarouselAdapter(imageUrls)
            imageCarousel.adapter = adapter
            startAutoCarousel(imageUrls.size)
        }
        
        // Setup sizes
        chipGroupSizes.removeAllViews()
        hasSize = !product.size_list.isNullOrEmpty()
        
        product.size_list?.forEachIndexed { index, size ->
            val chip = Chip(this).apply {
                text = "${size.name} (+${formatPrice(size.price.toString())}₫)"
                isCheckable = true
                setChipBackgroundColorResource(R.color.white)
                setChipStrokeColorResource(R.color.red_add_button)
                chipStrokeWidth = 2f
                
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSize = size.name
                        selectedSizePrice = size.price
                        updateTotalPrice()
                        
                        // Uncheck other chips
                        for (i in 0 until chipGroupSizes.childCount) {
                            val otherChip = chipGroupSizes.getChildAt(i) as? Chip
                            if (otherChip != this) {
                                otherChip?.isChecked = false
                            }
                        }
                    } else {
                        if (size.name == selectedSize) {
                            selectedSize = null
                            selectedSizePrice = 0
                            updateTotalPrice()
                        }
                    }
                }
            }
            chipGroupSizes.addView(chip)
            
            // Auto-select first size
            if (index == 0) {
                chip.isChecked = true
            }
        }
        
        // Setup toppings
        chipGroupToppings.removeAllViews()
        product.topping_list?.forEach { topping ->
            val chip = Chip(this).apply {
                text = "${topping.name} (+${formatPrice(topping.price)}₫)"
                isCheckable = true
                setChipBackgroundColorResource(R.color.white)
                setChipStrokeColorResource(R.color.red_add_button)
                chipStrokeWidth = 2f
                
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedToppings.add(topping)
                    } else {
                        selectedToppings.remove(topping)
                    }
                    updateTotalPrice()
                }
            }
            chipGroupToppings.addView(chip)
        }
        
        updateTotalPrice()
    }
    
    private fun startAutoCarousel(imageCount: Int) {
        if (imageCount <= 1) return
        
        carouselHandler.postDelayed(object : Runnable {
            override fun run() {
                currentImageIndex = (currentImageIndex + 1) % imageCount
                imageCarousel.setCurrentItem(currentImageIndex, true)
                carouselHandler.postDelayed(this, 3000)
            }
        }, 3000)
    }
    
    private fun updateQuantityAndPrice() {
        tvQuantity.text = quantity.toString()
        updateTotalPrice()
    }
    
    private fun updateTotalPrice() {
        val product = productDetail ?: return
        
        val basePrice = parsePrice(product.price)
        val toppingsPrice = selectedToppings.sumOf { parsePrice(it.price) }
        
        val totalPrice = (basePrice + selectedSizePrice + toppingsPrice) * quantity
        tvTotalPrice.text = formatPrice(totalPrice.toString()) + "₫"
    }
    
    private fun addToCart() {
        val product = productDetail ?: return
        
        val basePrice = parsePrice(product.price)
        val toppingsPrice = selectedToppings.sumOf { parsePrice(it.price) }
        val totalPrice = (basePrice + selectedSizePrice + toppingsPrice) * quantity
        val note = etNote.text.toString().trim()
        
        // Use selected size if available, otherwise check if product has sizes
        // If product has no sizes, send empty string, otherwise send first size as default
        val sizeToSend = if (product.size_list.isNullOrEmpty()) {
            "" // No sizes available
        } else {
            selectedSize ?: product.size_list.firstOrNull()?.name ?: ""
        }
        
        val productRequest = CartProductRequest(
            product_id = productId,
            total_price = totalPrice,
            size = sizeToSend,
            toppings_id = selectedToppings.map { it.id },
            note = note,
            quantity = quantity
        )
        
        val request = AddToCartRequest(productRequest)
        
        val dialog = ProgressDialog.show(this, null, "Đang thêm vào giỏ...", true, false)
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("ProductDetail", "Adding to cart: $request")
                val response = withContext(Dispatchers.IO) {
                    apiService.addProductToCart(request)
                }
                
                dialog.dismiss()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@ProductDetailActivity, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ProductDetail", "Add to cart failed: ${response.code()} - $errorBody")
                    val errorMsg = if (errorBody != null) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            json.optString("message", "Lỗi: ${response.code()}")
                        } catch (e: Exception) {
                            "Lỗi: ${response.code()}"
                        }
                    } else {
                        "Lỗi: ${response.code()}"
                    }
                    Toast.makeText(this@ProductDetailActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                dialog.dismiss()
                android.util.Log.e("ProductDetail", "Exception adding to cart", e)
                Toast.makeText(this@ProductDetailActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun parsePrice(price: String?): Int {
        if (price == null) return 0
        return try {
            price.toDouble().toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    private fun formatPrice(price: String?): String {
        val p = parsePrice(price)
        return String.format("%,d", p).replace(",", ".")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        carouselHandler.removeCallbacksAndMessages(null)
    }
}
