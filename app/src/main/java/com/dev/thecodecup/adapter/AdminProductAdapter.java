package com.dev.thecodecup.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.activity.AdminAddProductActivity;
import com.dev.thecodecup.model.network.dto.AdminProductDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {
    private final Context ctx;
    private final List<AdminProductDto> items = new ArrayList<>();
    private OnItemClickListener clickListener;
    private OnDeleteClickListener deleteListener;
    private boolean isToppingMode = false;

    public void setToppingMode(boolean topping) {
        isToppingMode = topping;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(AdminProductDto product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(AdminProductDto product, int position);
    }

    public AdminProductAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setItems(List<AdminProductDto> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_admin_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminProductDto p = items.get(position);
        h.txtProductName.setText(p.getProductName() != null ? p.getProductName() : "");
        String price = p.getProductPrice();
        if (price != null) {
            try {
                double d = Double.parseDouble(price);
                h.txtProductPrice.setText(new DecimalFormat("#,###").format(d) + "đ");
            } catch (NumberFormatException e) {
                h.txtProductPrice.setText(price + "đ");
            }
        } else {
            h.txtProductPrice.setText("");
        }

        h.txtProductStatus.setText("In stock");

        double rating = p.getAvgRating() != null ? p.getAvgRating() : 0.0;
        int reviewCount = p.getReviewCount() != null ? p.getReviewCount() : 0;
        h.txtProductRating.setText(String.format("%.1f★ (%d reviews)", rating, reviewCount));

        if (h.imgProduct != null) {
            h.imgProduct.setVisibility(View.VISIBLE);
            String imgUrl = p.getProductImageUrl();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(h.imgProduct.getContext())
                        .load(imgUrl)
                        .placeholder(com.dev.thecodecup.R.drawable.placeholder_image)
                        .error(com.dev.thecodecup.R.drawable.error_image)
                        .into(h.imgProduct);
                } catch (Exception e) {
                    h.imgProduct.setImageResource(com.dev.thecodecup.R.drawable.placeholder_image);
                }
            } else {
                h.imgProduct.setImageResource(com.dev.thecodecup.R.drawable.placeholder_image);
            }
        }

        // Edit button - launch AdminAddProductActivity in edit mode
        h.btnEditProduct.setOnClickListener(v -> {
            if (p.getProductId() != null) {
                Intent intent = new Intent(ctx, AdminAddProductActivity.class);
                intent.putExtra("productId", p.getProductId());
                ctx.startActivity(intent);
            }
        });
        
        h.btnDeleteProduct.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(p, position);
            }
        });
        
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtProductName, txtProductPrice, txtProductStatus, txtProductRating;
        ImageButton btnEditProduct, btnDeleteProduct;
        android.widget.ImageView imgProduct;
        VH(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductStatus = itemView.findViewById(R.id.txtProductStatus);
            txtProductRating = itemView.findViewById(R.id.txtProductRating);
            btnEditProduct = itemView.findViewById(R.id.btnEditProduct);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}
