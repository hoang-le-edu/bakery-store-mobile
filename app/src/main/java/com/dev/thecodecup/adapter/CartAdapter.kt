package com.dev.thecodecup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.thecodecup.R
import com.dev.thecodecup.model.network.api.CartOrderDetail

class CartAdapter(
    private val context: Context,
    private val onItemClick: (CartOrderDetail) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items = mutableListOf<CartOrderDetail>()

    fun setItems(newItems: List<CartOrderDetail>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
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
                txtNote.text = "Ghi chú: ${item.note}"
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
