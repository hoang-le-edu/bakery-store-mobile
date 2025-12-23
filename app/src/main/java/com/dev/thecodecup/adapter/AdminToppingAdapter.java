package com.dev.thecodecup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.AdminProductDto;

import java.util.ArrayList;
import java.util.List;

public class AdminToppingAdapter extends RecyclerView.Adapter<AdminToppingAdapter.VH> {
    private final Context ctx;
    private final List<AdminProductDto> items = new ArrayList<>();

    public AdminToppingAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setItems(List<AdminProductDto> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminProductDto p = items.get(position);
        h.txtName.setText(p.getProductName() != null ? p.getProductName() : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName;
        VH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(android.R.id.text1);
        }
    }
}
