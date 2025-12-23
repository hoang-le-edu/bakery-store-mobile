package com.dev.thecodecup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.OrderDetailItem;
import com.dev.thecodecup.model.network.dto.OrderDetailTopping;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailProductsAdapter extends RecyclerView.Adapter<OrderDetailProductsAdapter.ProductViewHolder> {

    private final List<OrderDetailItem> products = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public void setProducts(List<OrderDetailItem> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtSize;
        private final TextView txtQuantity;
        private final TextView txtPrice;
        private final TextView txtToppings;
        private final TextView txtNote;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtSize = itemView.findViewById(R.id.txtSize);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtToppings = itemView.findViewById(R.id.txtToppings);
            txtNote = itemView.findViewById(R.id.txtNote);
        }

        public void bind(OrderDetailItem product) {
            // Product name
            txtProductName.setText(product.getProductName() != null ? product.getProductName() : "N/A");

            // Size
            txtSize.setText("Size: " + (product.getSize() != null ? product.getSize() : "N/A"));

            // Quantity
            txtQuantity.setText("x" + (product.getQuantity() != null ? product.getQuantity() : 0));

            // Price
            if (product.getTotalPrice() != null) {
                try {
                    int price = (int) Double.parseDouble(product.getTotalPrice());
                    txtPrice.setText(formatter.format(price) + "₫");
                } catch (NumberFormatException e) {
                    txtPrice.setText(product.getTotalPrice() + "₫");
                }
            } else {
                txtPrice.setText("0₫");
            }

            // Product image
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImage())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_image);
            }

            // Toppings
            List<OrderDetailTopping> toppings = product.getToppings();
            if (toppings != null && !toppings.isEmpty()) {
                StringBuilder toppingText = new StringBuilder("Toppings: ");
                for (int i = 0; i < toppings.size(); i++) {
                    if (i > 0) toppingText.append(", ");
                    toppingText.append(toppings.get(i).getName());
                }
                txtToppings.setText(toppingText.toString());
                txtToppings.setVisibility(View.VISIBLE);
            } else {
                txtToppings.setVisibility(View.GONE);
            }

            // Note
            if (product.getNote() != null && !product.getNote().isEmpty()) {
                txtNote.setText("Note: " + product.getNote());
                txtNote.setVisibility(View.VISIBLE);
            } else {
                txtNote.setVisibility(View.GONE);
            }
        }
    }
}
