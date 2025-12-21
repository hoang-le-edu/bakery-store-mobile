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
import com.dev.thecodecup.model.network.dto.AdminOrderItemDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdminOrderDetailItemAdapter extends RecyclerView.Adapter<AdminOrderDetailItemAdapter.OrderItemViewHolder> {

    private final List<AdminOrderItemDto> items = new ArrayList<>();

    public void setItems(List<AdminOrderItemDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order_detail_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvVariant;
        TextView tvPrice;
        TextView tvNote;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvNote = itemView.findViewById(R.id.tvNote);
        }

        void bind(AdminOrderItemDto item) {
            tvProductName.setText(item.getProductName() != null ? item.getProductName() : "Unknown");

            // Build variant string: Size S • x1 • X topping
            StringBuilder variantBuilder = new StringBuilder();
            if (item.getSize() != null && !item.getSize().isEmpty()) {
                variantBuilder.append("Size ").append(item.getSize());
            }
            variantBuilder.append(" • x").append(item.getQuantity() != null ? item.getQuantity() : 0);
            
            Integer toppingCount = item.getCountTopping();
            if (toppingCount != null && toppingCount > 0) {
                variantBuilder.append(" • ").append(toppingCount).append(" topping");
            } else if (item.getToppings() != null && !item.getToppings().isEmpty()) {
                variantBuilder.append(" • ").append(item.getToppings().size()).append(" topping");
            }
            
            tvVariant.setText(variantBuilder.toString());

            // Price
            String priceStr = item.getTotalPrice() != null ? item.getTotalPrice() : item.getProductPrice();
            tvPrice.setText(formatCurrency(priceStr));

            // Note
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText("Note: " + item.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Load image
            Glide.with(itemView.getContext())
                    .load(item.getImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imgProduct);
        }

        private String formatCurrency(String value) {
            if (value == null || value.isEmpty()) return "0đ";
            try {
                double number = Double.parseDouble(value);
                DecimalFormat formatter = new DecimalFormat("###,###,###");
                return formatter.format(number) + "đ";
            } catch (Exception e) {
                return value + "đ";
            }
        }
    }
}
