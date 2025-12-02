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
import com.dev.thecodecup.model.network.dto.OrderDto;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private final List<OrderDto> items = new ArrayList<>();

    public void setItems(List<OrderDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
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
        OrderDto order = items.get(position);

        holder.tvOrderId.setText("#" + order.getOrderId());
        holder.tvReceiverName.setText(order.getReceiverName());
        holder.tvOrderTime.setText(order.getOrderTime());
        holder.tvOrderTotal.setText(String.format("%.0fđ", order.getTotal()));

        String status = order.getOrderStatus();
        holder.tvOrderStatus.setText(status.replace("_", " "));

        // ---- set màu theo status, dùng 1 shape chung ----
        GradientDrawable bg = (GradientDrawable) holder.tvOrderStatus.getBackground().mutate();

        int color;
        switch (status) {
            case "PENDING":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
                break;
            case "ON_GOING":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_ongoing);
                break;
            case "CANCELLED":
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_cancelled);
                break;
            default: // COMPLETED, v.v.
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_completed);
                break;
        }
        bg.setColor(color);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardRoot;
        TextView tvOrderId, tvReceiverName, tvOrderStatus, tvOrderTime, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = (CardView) itemView;
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
