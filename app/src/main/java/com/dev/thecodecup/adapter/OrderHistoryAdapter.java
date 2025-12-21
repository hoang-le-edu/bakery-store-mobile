package com.dev.thecodecup.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.api.Order;
import com.dev.thecodecup.model.network.api.OrderDetail;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);

        void onPayNowClick(Order order);
    }

    public OrderHistoryAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvOrderDate, tvOrderStatus, tvItemCount, tvPaymentMethod, tvTotalPrice, tvProductName,
                tvProductVariant;
        ImageView ivProductImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tv_order_number);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductVariant = itemView.findViewById(R.id.tv_product_variant); // New TextView
            ivProductImage = itemView.findViewById(R.id.iv_product_image);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onOrderClick(orders.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Order order) {
            tvOrderNumber.setText(order.getOrder_number());

            // Format date if needed, currently using raw string
            // Extract just the date part if it's ISO format
            String dateStr = order.getOrder_date() != null ? order.getOrder_date() : "";
            if (dateStr.contains("T")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("T"));
            }
            tvOrderDate.setText(dateStr);

            // Ưu tiên lấy status từ trường 'status' (API trả về), nếu null thì lấy
            // 'order_status'
            String status = order.getStatus();
            if (status == null) {
                status = order.getOrder_status();
            }
            if (status == null) {
                status = "Unknown";
            }

            tvOrderStatus.setText(status);

            int color;
            switch (status) {
                case "Completed":
                    color = Color.parseColor("#4CAF50"); // Green
                    break;
                case "Cancelled":
                    color = Color.parseColor("#F44336"); // Red
                    break;
                case "Wait For Approval":
                    color = Color.parseColor("#FF9800"); // Orange
                    break;
                case "In Progress":
                    color = Color.parseColor("#2196F3"); // Blue
                    break;
                default:
                    color = Color.BLACK;
            }
            tvOrderStatus.setTextColor(color);

            // Item count
            String itemCountText = order.getCount_product() + " sản phẩm";
            tvItemCount.setText(itemCountText);

            // Payment method
            tvPaymentMethod
                    .setText("Thanh toán: " + (order.getPayment_method() != null ? order.getPayment_method() : ""));

            // Total price
            String priceStr = order.getOrder_total();
            if (priceStr == null && order.getTotal_price() != null) {
                priceStr = order.getTotal_price();
            }

            if (priceStr != null) {
                try {
                    double price = Double.parseDouble(priceStr);
                    DecimalFormat formatter = new DecimalFormat("###,###,###");
                    tvTotalPrice.setText(formatter.format(price) + "₫");
                } catch (NumberFormatException e) {
                    tvTotalPrice.setText(priceStr + "₫");
                }
            } else {
                tvTotalPrice.setText("0₫");
            }

            // Display Product Image and Name from the first order detail
            if (order.getOrder_detail() != null && !order.getOrder_detail().isEmpty()) {
                OrderDetail firstItem = order.getOrder_detail().get(0);
                tvProductName.setText(firstItem.getProduct_name());

                // Construct variant string: Size: S • x1 • 0 Topping
                StringBuilder variantBuilder = new StringBuilder();
                if (firstItem.getSize() != null && !firstItem.getSize().isEmpty()) {
                    variantBuilder.append("Size: ").append(firstItem.getSize());
                }

                variantBuilder.append(" • x").append(firstItem.getQuantity());

                if (firstItem.getCount_topping() > 0) {
                    variantBuilder.append(" • ").append(firstItem.getCount_topping()).append(" Topping");
                } else if (firstItem.getToppings() != null && !firstItem.getToppings().isEmpty()) {
                    variantBuilder.append(" • ").append(firstItem.getToppings().size()).append(" Topping");
                }

                tvProductVariant.setText(variantBuilder.toString());
                tvProductVariant.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(firstItem.getImage())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivProductImage);
            } else {
                tvProductName.setText("Chi tiết đơn hàng");
                tvProductVariant.setVisibility(View.GONE);
                ivProductImage.setImageResource(R.drawable.placeholder_image);
            }

            // Show Pay Now button for Banking orders with pending payment
            boolean isBanking = "Banking".equalsIgnoreCase(order.getPayment_method());
            boolean isPending = "pending".equalsIgnoreCase(order.getPayment_status());

            if (isBanking && isPending) {
                btnPayNow.setVisibility(View.VISIBLE);
            } else {
                btnPayNow.setVisibility(View.GONE);
            }
        }
    }
}
