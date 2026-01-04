package com.dev.thecodecup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.thecodecup.R
import com.dev.thecodecup.model.db.cart.CartItemEntity
import com.google.android.material.checkbox.MaterialCheckBox

class OfflineCartAdapter(
    private val context: Context,
    private val onItemEdit: (CartItemEntity) -> Unit,
    private val onQuantityChange: (CartItemEntity, Int) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<OfflineCartAdapter.CartItemViewHolder>() {
    
    private val cartItems: MutableList<CartItemEntity> = mutableListOf()
    private val selectedItems: MutableSet<Int> = mutableSetOf()
    
    fun updateItems(items: List<CartItemEntity>) {
        cartItems.clear()
        cartItems.addAll(items)
        selectedItems.retainAll(items.map { it.id }.toSet()) // Remove selections for items that no longer exist
        notifyDataSetChanged()
    }
    
    fun getSelectedItems(): List<CartItemEntity> {
        return cartItems.filter { selectedItems.contains(it.id) }
    }
    
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(cartItems.map { it.id })
        notifyDataSetChanged()
        onSelectionChanged()
    }
    
    fun deselectAll() {
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }
    
    fun isAllSelected(): Boolean {
        return cartItems.isNotEmpty() && selectedItems.size == cartItems.size
    }
    
    fun getItemAtPosition(position: Int): CartItemEntity {
        return cartItems[position]
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_offline_cart, parent, false)
        return CartItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)
    }
    
    override fun getItemCount(): Int = cartItems.size
    
    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkboxSelect: MaterialCheckBox = itemView.findViewById(R.id.checkboxSelect)
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        private val txtProductPrice: TextView = itemView.findViewById(R.id.txtProductPrice)
        private val txtSyncStatus: TextView = itemView.findViewById(R.id.txtSyncStatus)
        private val btnDecrease: ImageButton = itemView.findViewById(R.id.btnDecrease)
        private val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        private val btnIncrease: ImageButton = itemView.findViewById(R.id.btnIncrease)
        private val txtTotalPrice: TextView = itemView.findViewById(R.id.txtTotalPrice)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        
        fun bind(item: CartItemEntity) {
            // Set checkbox state
            checkboxSelect.setOnCheckedChangeListener(null) // Prevent recursive calls
            checkboxSelect.isChecked = selectedItems.contains(item.id)
            checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(item.id)
                } else {
                    selectedItems.remove(item.id)
                }
                onSelectionChanged()
            }
            
            // Load product image
            if (!item.productImage.isNullOrEmpty()) {
                Glide.with(context)
                    .load(item.productImage)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imgProduct)
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_image)
            }
            
            // Set product info
            txtProductName.text = item.productName ?: "Loading..."
            txtProductPrice.text = "$${String.format("%.2f", item.unitPrice)}"
            
            // Set sync status
            updateSyncStatus(item)
            
            // Set quantity
            txtQuantity.text = item.quantity.toString()
            
            // Set total price
            txtTotalPrice.text = "$${String.format("%.2f", item.totalPrice)}"
            
            // Setup quantity buttons
            btnDecrease.setOnClickListener {
                val newQuantity = item.quantity - 1
                if (newQuantity > 0) {
                    onQuantityChange(item, newQuantity)
                } else {
                    onQuantityChange(item, 0) // This will trigger removal
                }
            }
            
            btnIncrease.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityChange(item, newQuantity)
            }
            
            // Setup edit button
            btnEdit.setOnClickListener {
                onItemEdit(item)
            }
            
            // Enable/disable based on product availability
            val isAvailable = item.isProductAvailable ?: true
            itemView.alpha = if (isAvailable) 1.0f else 0.6f
            btnDecrease.isEnabled = isAvailable
            btnIncrease.isEnabled = isAvailable
            btnEdit.isEnabled = isAvailable
            
            if (!isAvailable) {
                txtSyncStatus.text = "Product unavailable"
                txtSyncStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                txtSyncStatus.visibility = View.VISIBLE
            }
        }
        
        private fun updateSyncStatus(item: CartItemEntity) {
            // Show sync status based on server timestamp (null means not yet synced)
            if (item.serverTimestamp == null) {
                txtSyncStatus.text = "Pending sync"
                txtSyncStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                txtSyncStatus.visibility = View.VISIBLE
            } else {
                // Item is synced
                txtSyncStatus.text = "Synced"
                txtSyncStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                txtSyncStatus.visibility = View.GONE // Hide when synced
            }
        }
    }
}