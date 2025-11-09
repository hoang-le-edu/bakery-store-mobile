package com.dev.thecodecup.adapter;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.model.network.dto.ProductDto;


import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context context;
    private List<ProductDto> productList;

    public ProductAdapter(Context context, List<ProductDto> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductDto p = productList.get(position);
        holder.tvName.setText(p.getProductName());
        holder.tvPrice.setText((int) p.getPrice());
        if (p.getProductImage() != null && !p.getProductImage().isEmpty()) {
            Glide.with(context)
                    .load(p.getProductImage())
                    .placeholder(R.drawable.placeholder_image) // ảnh mặc định khi đang tải
                    .error(R.drawable.error_image)             // ảnh khi lỗi tải
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName  = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
