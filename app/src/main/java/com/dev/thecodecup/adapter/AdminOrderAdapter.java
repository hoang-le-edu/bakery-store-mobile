package com.dev.thecodecup.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.AdminOrderDto;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(AdminOrderDto order);
    }

    private final List<AdminOrderDto> items = new ArrayList<>();
    private OnOrderClickListener listener;

    public void setItems(List<AdminOrderDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        AdminOrderDto order = items.get(position);

        // Order Number
        holder.tvOrderId.setText(order.getOrderNumber() != null ? order.getOrderNumber() : "N/A");
        
        // Receiver Name
        holder.tvReceiverName.setText(order.getReceiverName() != null ? order.getReceiverName() : "N/A");
        
        // Created At
        holder.tvOrderTime.setText(order.getCreatedAt() != null ? order.getCreatedAt() : "N/A");
        
        // Order Total
        if (order.getOrderTotal() != null) {
            try {
                double total = Double.parseDouble(order.getOrderTotal());
                holder.tvOrderTotal.setText(String.format("%.0fđ", total));
            } catch (NumberFormatException e) {
                holder.tvOrderTotal.setText(order.getOrderTotal() + "đ");
            }
        } else {
            holder.tvOrderTotal.setText("0đ");
        }
        
        // Receiver Address
        holder.tvReceiverAddress.setText(order.getReceiverAddress() != null && !order.getReceiverAddress().isEmpty() 
            ? order.getReceiverAddress() 
            : "No address provided");
        
        // Payment Method
        holder.tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A");
        
        // Payment Status
        String paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : "pending";
        holder.tvPaymentStatus.setText(paymentStatus);
        
        // Set payment status color
        GradientDrawable paymentBg = (GradientDrawable) holder.tvPaymentStatus.getBackground().mutate();
        int paymentColor;
        if ("paid".equalsIgnoreCase(paymentStatus) || "completed".equalsIgnoreCase(paymentStatus)) {
            paymentColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_completed);
        } else if ("pending".equalsIgnoreCase(paymentStatus)) {
            paymentColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        } else {
            paymentColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_cancelled);
        }
        paymentBg.setColor(paymentColor);

        // Order Status
        String status = order.getOrderStatus() != null ? order.getOrderStatus() : "Draft";
        holder.tvOrderStatus.setText(status);

        // Set order status color
        GradientDrawable bg = (GradientDrawable) holder.tvOrderStatus.getBackground().mutate();
        int color;
        switch (status) {
            case "Wait For Approval":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
                break;
            case "In Progress":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_ongoing);
                break;
            case "Delivering":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_ongoing);
                break;
            case "Completed":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_completed);
                break;
            case "Cancelled":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_cancelled);
                break;
            default: // Draft, etc.
                color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray);
                break;
        }
        bg.setColor(color);

        holder.cardRoot.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardRoot;
        TextView tvOrderId, tvReceiverName, tvOrderStatus, tvOrderTime, tvOrderTotal;
        TextView tvReceiverAddress, tvPaymentMethod, tvPaymentStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = (CardView) itemView;
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvReceiverAddress = itemView.findViewById(R.id.tvReceiverAddress);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
        }
    }
}
