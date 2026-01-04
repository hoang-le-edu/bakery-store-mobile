package com.dev.thecodecup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.CategoryDto;

import java.util.List;

public class SelectedCategoryAdapter extends RecyclerView.Adapter<SelectedCategoryAdapter.ViewHolder> {

    private final List<CategoryDto> categories;
    private final OnRemoveListener onRemoveListener;

    public interface OnRemoveListener {
        void onRemove(CategoryDto category);
    }

    public SelectedCategoryAdapter(List<CategoryDto> categories, OnRemoveListener listener) {
        this.categories = categories;
        this.onRemoveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDto category = categories.get(position);
        holder.tvCategoryName.setText(category.getCategoryName());
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRemoveListener != null) {
                    onRemoveListener.onRemove(category);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
