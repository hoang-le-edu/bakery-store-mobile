package com.dev.thecodecup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.AdminCustomerOrderDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdminCustomerOrderAdapter extends RecyclerView.Adapter<AdminCustomerOrderAdapter.ViewHolder> {

    private List<AdminCustomerOrderDto> items = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(AdminCustomerOrderDto order);
    }

    public void setItems(List<AdminCustomerOrderDto> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_customer_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminCustomerOrderDto order = items.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber;
        TextView tvOrderStatus;
        TextView tvOrderTotal;
        TextView tvPaymentMethod;
        TextView tvCreatedAt;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            cardView = itemView.findViewById(R.id.cardView);
        }

        void bind(AdminCustomerOrderDto order) {
            tvOrderNumber.setText(order.getOrderNumber() != null ? order.getOrderNumber() : "#");
            tvOrderStatus.setText(order.getOrderStatus() != null ? order.getOrderStatus() : "--");
            tvOrderTotal.setText(formatCurrency(order.getOrderTotal()));
            tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "--");
            tvCreatedAt.setText(order.getCreatedAt() != null ? formatDate(order.getCreatedAt()) : "--");

            applyStatusColor(tvOrderStatus, order.getOrderStatus());

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private String formatCurrency(String value) {
            if (value == null || value.isEmpty()) return "0đ";
            try {
                String normalized = value.trim().replace("đ", "").replace(",", "");
                double num = Double.parseDouble(normalized);
                DecimalFormat formatter = new DecimalFormat("###,###,###");
                return formatter.format(num) + "đ";
            } catch (Exception e) {
                return value + "đ";
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null) return "--";
            try {
                // Format: "2025-12-22T05:45:02.000000Z" -> "22/12/2025"
                String[] parts = dateString.split("T");
                if (parts.length > 0) {
                    String[] dateParts = parts[0].split("-");
                    if (dateParts.length == 3) {
                        return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                    }
                }
            } catch (Exception e) {
                // Return original if parsing fails
            }
            return dateString;
        }

        private void applyStatusColor(TextView tvStatus, String status) {
            int color;
            if (status == null) {
                color = itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            } else if ("Wait For Approval".equalsIgnoreCase(status)) {
                color = itemView.getContext().getResources().getColor(R.color.status_waitForApproval);
            } else if ("In Progress".equalsIgnoreCase(status) || "Delivering".equalsIgnoreCase(status)) {
                color = itemView.getContext().getResources().getColor(R.color.status_inProgress);
            } else if ("Completed".equalsIgnoreCase(status)) {
                color = itemView.getContext().getResources().getColor(R.color.status_completed);
            } else if ("Cancelled".equalsIgnoreCase(status)) {
                color = itemView.getContext().getResources().getColor(R.color.status_cancelled);
            } else {
                color = itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            }
            tvStatus.setBackgroundResource(R.drawable.bg_order_status);
            android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) tvStatus.getBackground().mutate();
            bg.setColor(color);
        }
    }
}
