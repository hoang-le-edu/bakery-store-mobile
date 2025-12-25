package com.dev.thecodecup.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.thecodecup.R
import com.dev.thecodecup.model.network.api.CartOrderDetail
import com.google.android.material.checkbox.MaterialCheckBox

class CartAdapter(
    private val context: Context,
    private val onItemClick: (CartOrderDetail) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items = mutableListOf<CartOrderDetail>()
    private val selectedItems = mutableSetOf<String>()

    fun setItems(newItems: List<CartOrderDetail>) {
        items.clear()
        items.addAll(newItems)
        // Auto-select all items by default
        selectedItems.clear()
        items.forEach { selectedItems.add(it.id) }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun getSelectedItems(): List<CartOrderDetail> {
        val selected = items.filter { selectedItems.contains(it.id) }
        Log.d("CartAdapter", "getSelectedItems called: ${selected.size} items selected out of ${items.size}")
        return selected
    }

    fun selectAll() {
        selectedItems.clear()
        items.forEach { selectedItems.add(it.id) }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun deselectAll() {
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun isAllSelected(): Boolean {
        return items.isNotEmpty() && selectedItems.size == items.size
    }

    fun getItem(position: Int): CartOrderDetail? {
        return if (position >= 0 && position < items.size) {
            items[position]
        } else {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkboxSelect: MaterialCheckBox = itemView.findViewById(R.id.checkboxSelect)
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        private val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        private val sizeLayout: View = itemView.findViewById(R.id.sizeLayout)
        private val txtSize: TextView = itemView.findViewById(R.id.txtSize)
        private val toppingsLayout: View = itemView.findViewById(R.id.toppingsLayout)
        private val txtToppings: TextView = itemView.findViewById(R.id.txtToppings)
        private val noteLayout: View = itemView.findViewById(R.id.noteLayout)
        private val txtNote: TextView = itemView.findViewById(R.id.txtNote)

        fun bind(item: CartOrderDetail) {
            // Remove listener first to avoid triggering during setup
            checkboxSelect.setOnCheckedChangeListener(null)
            
            // Set checkbox state
            checkboxSelect.isChecked = selectedItems.contains(item.id)
            
            // Set listener after state is set
            checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                Log.d("CartAdapter", "Checkbox changed for ${item.product_name}: $isChecked")
                if (isChecked) {
                    selectedItems.add(item.id)
                    Log.d("CartAdapter", "Added ${item.id}, total selected: ${selectedItems.size}")
                } else {
                    selectedItems.remove(item.id)
                    Log.d("CartAdapter", "Removed ${item.id}, total selected: ${selectedItems.size}")
                }
                Log.d("CartAdapter", "Calling onSelectionChanged callback")
                onSelectionChanged()
            }
            
            txtProductName.text = item.product_name
            txtQuantity.text = item.quantity.toString()

            // Size
            if (!item.size.isNullOrEmpty()) {
                sizeLayout.visibility = View.VISIBLE
                txtSize.text = "Size: ${item.size}"
            } else {
                sizeLayout.visibility = View.GONE
            }

            // Toppings
            if (item.toppings.isNotEmpty()) {
                toppingsLayout.visibility = View.VISIBLE
                val toppingNames = item.toppings.joinToString(", ") { it.name }
                txtToppings.text = "Topping: $toppingNames"
            } else {
                toppingsLayout.visibility = View.GONE
            }

            // Note
            if (!item.note.isNullOrEmpty()) {
                noteLayout.visibility = View.VISIBLE
                txtNote.text = "Note: ${item.note}"
            } else {
                noteLayout.visibility = View.GONE
            }

            txtPrice.text = formatPrice(item.total_price) + "₫"

            // Load image
            Glide.with(context)
                .load(item.image)
                .placeholder(R.drawable.img_placeholder) // Corrected placeholder
                .error(R.drawable.error_image) // Corrected error drawable
                .centerCrop()
                .into(imgProduct)

            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun formatPrice(price: String?): String {
            if (price == null) return "0"
            return try {
                val p = price.toDouble().toInt()
                String.format("%,d", p).replace(",", ".")
            } catch (e: Exception) {
                price
            }
        }
    }
}
